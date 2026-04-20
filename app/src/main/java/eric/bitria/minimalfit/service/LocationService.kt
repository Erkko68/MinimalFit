package eric.bitria.minimalfit.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import eric.bitria.minimalfit.MainActivity
import eric.bitria.minimalfit.R
import eric.bitria.minimalfit.data.track.RecordingState
import eric.bitria.minimalfit.data.track.TrackingLogic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.time.Duration

class LocationService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        
        private const val CHANNEL_ID = "location_channel"
        private const val NOTIFICATION_ID = 1
    }

    private val trackingLogic: TrackingLogic by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val binder = LocalBinder()
    private var isBound = false
    private var isForegroundService = false

    inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Sync notification with TrackingLogic state
        serviceScope.launch {
            launch {
                trackingLogic.recordingState.collect { state ->
                    if (isForegroundService) updateNotification()
                    if (state == RecordingState.IDLE && isForegroundService) {
                        stopTracking()
                    }
                }
            }
            launch {
                trackingLogic.duration.collect { if (isForegroundService) updateNotification() }
            }
            launch {
                trackingLogic.distanceKm.collect { if (isForegroundService) updateNotification() }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_PAUSE -> trackingLogic.pause()
            ACTION_RESUME -> trackingLogic.start()
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        isBound = true
        return binder
    }

    override fun onRebind(intent: Intent?) {
        isBound = true
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isBound = false
        return true
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startTracking() {
        trackingLogic.start()
        promoteToForeground()
    }

    private fun stopTracking() {
        trackingLogic.stop()
        demoteToBackground()
        stopSelf()
    }

    private fun promoteToForeground() {
        if (isForegroundService) return
        isForegroundService = true

        val notification = buildNotification()

        startForeground(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )
    }

    private fun demoteToBackground() {
        if (!isForegroundService) return
        isForegroundService = false
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun updateNotification() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Location Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Shows tracking stats in background" }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val state = trackingLogic.recordingState.value
        val isPaused = state == RecordingState.PAUSED

        val pauseResumeAction = if (isPaused) {
            val resumeIntent = PendingIntent.getService(
                this, 2,
                Intent(this, LocationService::class.java).apply { action = ACTION_RESUME },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action(0, "Resume", resumeIntent)
        } else {
            val pauseIntent = PendingIntent.getService(
                this, 3,
                Intent(this, LocationService::class.java).apply { action = ACTION_PAUSE },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action(0, "Pause", pauseIntent)
        }

        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, LocationService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val distance = trackingLogic.distanceKm.value
        val duration = trackingLogic.duration.value
        
        val contentText = "Distance: %.2f km | Time: %s".format(
            distance,
            formatDuration(duration)
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (isPaused) "Tracking Paused" else "Tracking Active")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(pauseResumeAction)
            .addAction(0, "Stop", stopIntent)
            .build()
    }

    private fun formatDuration(duration: Duration): String {
        val seconds = duration.inWholeSeconds
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, secs)
        } else {
            "%02d:%02d".format(minutes, secs)
        }
    }
}
