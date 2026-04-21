package eric.bitria.minimalfit.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import eric.bitria.minimalfit.MainActivity
import eric.bitria.minimalfit.R
import eric.bitria.minimalfit.data.track.RecordingState
import eric.bitria.minimalfit.data.track.TrackingLogic
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.time.Duration

/**
 * LocationService manages location tracking as a Foreground Service.
 *
 * Lifecycle:
 *  - ACTION_START  → starts TrackingLogic + shows persistent notification
 *  - ACTION_PAUSE  → pauses TrackingLogic, notification reflects paused state
 *  - ACTION_RESUME → resumes TrackingLogic, notification reflects active state
 *  - ACTION_STOP   → stops TrackingLogic, removes notification, stops service
 *
 * The notification updates automatically whenever recording state, duration,
 * or distance changes. The service stops itself when RecordingState returns
 * to IDLE (e.g. after an internal stop from TrackingLogic).
 *
 * This architecture is crash-free on Android 14+ (foreground service type
 * declared explicitly as FOREGROUND_SERVICE_TYPE_LOCATION).
 */
class LocationService : LifecycleService() {

    companion object {
        const val ACTION_START  = "ACTION_START"
        const val ACTION_PAUSE  = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP   = "ACTION_STOP"

        private const val CHANNEL_ID       = "location_channel"
        private const val NOTIFICATION_ID  = 1

        // Stable request codes for PendingIntents — must be unique per action
        private const val REQUEST_OPEN   = 0
        private const val REQUEST_STOP   = 1
        private const val REQUEST_RESUME = 2
        private const val REQUEST_PAUSE  = 3
    }

    private val trackingLogic: TrackingLogic by inject()

    /** True while the foreground service (and its notification) is active. */
    private var isForeground = false

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        observeTrackingState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START  -> startTracking()
            ACTION_PAUSE  -> trackingLogic.pause()
            ACTION_RESUME -> trackingLogic.start()
            ACTION_STOP   -> stopTracking()
        }
        return START_NOT_STICKY
    }

    /** Not a bound service — binding is unsupported. */
    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    /**
     * If the user swipes the app away while tracking is NOT active,
     * there is nothing keeping the service alive, so clean it up.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (!isForeground) stopSelf()
    }

    // -------------------------------------------------------------------------
    // Tracking control
    // -------------------------------------------------------------------------

    private fun startTracking() {
        trackingLogic.start()
        showForegroundNotification()
    }

    private fun stopTracking() {
        trackingLogic.stop()
        removeForegroundNotification()
        stopSelf()
    }

    // -------------------------------------------------------------------------
    // State observation
    // -------------------------------------------------------------------------

    /**
     * Combines all three tracking flows so the notification refreshes whenever
     * any of them changes (state, elapsed time, or distance).
     */
    private fun observeTrackingState() {
        lifecycleScope.launch {
            combine(
                trackingLogic.recordingState,
                trackingLogic.duration,
                trackingLogic.distanceKm
            ) { state, duration, km -> Triple(state, duration, km) }
                .collect { (state, _, _) ->
                    if (isForeground) {
                        updateNotification()
                        if (state == RecordingState.IDLE) {
                            stopTracking()
                        }
                    }
                }
        }
    }

    // -------------------------------------------------------------------------
    // Foreground notification management
    // -------------------------------------------------------------------------

    private fun showForegroundNotification() {
        if (isForeground) return
        isForeground = true
        startForeground(
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )
    }

    private fun removeForegroundNotification() {
        if (!isForeground) return
        isForeground = false
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun updateNotification() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    // -------------------------------------------------------------------------
    // Notification building
    // -------------------------------------------------------------------------

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Location Tracking",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Shows live tracking stats while a workout is in progress"
        }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val isPaused = trackingLogic.recordingState.value == RecordingState.PAUSED

        val contentText = "%.2f km  ·  %s".format(
            trackingLogic.distanceKm.value,
            formatDuration(trackingLogic.duration.value)
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (isPaused) "Tracking paused" else "Tracking active")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(buildOpenAppIntent())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(buildPauseResumeAction(isPaused))
            .addAction(buildStopAction())
            .build()
    }

    private fun buildOpenAppIntent(): PendingIntent =
        PendingIntent.getActivity(
            this,
            REQUEST_OPEN,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun buildPauseResumeAction(isPaused: Boolean): NotificationCompat.Action {
        return if (isPaused) {
            val intent = serviceIntent(ACTION_RESUME)
            val pending = PendingIntent.getService(
                this, REQUEST_RESUME, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action(0, "Resume", pending)
        } else {
            val intent = serviceIntent(ACTION_PAUSE)
            val pending = PendingIntent.getService(
                this, REQUEST_PAUSE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action(0, "Pause", pending)
        }
    }

    private fun buildStopAction(): NotificationCompat.Action {
        val pending = PendingIntent.getService(
            this, REQUEST_STOP, serviceIntent(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action(0, "Stop", pending)
    }

    private fun serviceIntent(action: String): Intent =
        Intent(this, LocationService::class.java).apply { this.action = action }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun formatDuration(duration: Duration): String {
        val totalSeconds = duration.inWholeSeconds
        val hours   = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }
}