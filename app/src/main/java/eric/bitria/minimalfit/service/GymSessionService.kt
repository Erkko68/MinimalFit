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
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.gym.GymTrackingLogic
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.time.Duration

/**
 * GymSessionService manages gym workout tracking as a Foreground Service.
 *
 * Lifecycle:
 *  - ACTION_START       → starts GymTrackingLogic + shows persistent notification
 *  - ACTION_FINISH      → completes the session, removes notification, stops service
 *  - ACTION_START_REST  → starts/adds to a rest timer (EXTRA_SECONDS)
 *  - ACTION_STOP_REST   → cancels the current rest timer
 */
class GymSessionService : LifecycleService() {

    companion object {
        const val ACTION_START                = "ACTION_START"
        const val ACTION_FINISH               = "ACTION_FINISH"
        const val ACTION_START_REST           = "ACTION_START_REST"
        const val ACTION_STOP_REST            = "ACTION_STOP_REST"

        const val EXTRA_EXERCISE_ID = "extra_exercise_id"
        const val EXTRA_SECONDS     = "extra_seconds"

        private const val CHANNEL_ID      = "gym_session_channel"
        private const val NOTIFICATION_ID = 2

        private const val REQUEST_OPEN           = 0
        private const val REQUEST_FINISH_WORKOUT = 1
        private const val REQUEST_STOP_REST      = 2
        private const val REQUEST_START_REST      = 3
    }

    private val trackingLogic: GymTrackingLogic by inject()

    private var isForeground = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        observeTrackingState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START  -> startSession()
            ACTION_FINISH -> finishSession()
            ACTION_START_REST -> {
                val seconds = intent.getIntExtra(EXTRA_SECONDS, 60)
                trackingLogic.startRest(seconds)
            }
            ACTION_STOP_REST  -> trackingLogic.stopRest()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (!isForeground) stopSelf()
    }

    private fun startSession() {
        trackingLogic.start()
        showForegroundNotification()
    }

    private fun finishSession() {
        trackingLogic.finish()
        stopSessionService()
    }

    private fun stopSessionService() {
        removeForegroundNotification()
        stopSelf()
    }

    private fun observeTrackingState() {
        lifecycleScope.launch {
            combine(
                trackingLogic.activeSession,
                trackingLogic.elapsed,
                trackingLogic.restRemaining,
                trackingLogic.isRestRunning
            ) { activeSession, _, _, _ ->
                activeSession
            }.collect { session ->
                if (isForeground) {
                    updateNotification()
                    if (session == null) {
                        stopSessionService()
                    }
                }
            }
        }
    }

    private fun showForegroundNotification() {
        if (isForeground) return
        isForeground = true
        startForeground(
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
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

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Gym Session",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Shows your workout timer while the app is in the background"
        }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val elapsedText    = formatDuration(trackingLogic.elapsed.value)
        val restRunning    = trackingLogic.isRestRunning.value
        val restText       = formatDuration(trackingLogic.restRemaining.value)

        val contentText = if (restRunning) {
            "Rest: $restText  ·  Workout: $elapsedText"
        } else {
            "Time: $elapsedText"
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (restRunning) "Rest timer" else "Workout in progress")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(buildOpenAppIntent())
            .setOnlyAlertOnce(true)
            .setOngoing(true)

        if (restRunning) {
            builder.addAction(0, "+30s", buildServiceIntent(ACTION_START_REST, REQUEST_START_REST) {
                putExtra(EXTRA_SECONDS, 30)
            })
            builder.addAction(0, "Stop Rest", buildServiceIntent(ACTION_STOP_REST, REQUEST_STOP_REST))
        }
        builder.addAction(0, "Finish Workout", buildServiceIntent(ACTION_FINISH, REQUEST_FINISH_WORKOUT))

        return builder.build()
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

    private fun buildServiceIntent(
        action: String,
        requestCode: Int,
        extras: Intent.() -> Unit = {}
    ): PendingIntent =
        PendingIntent.getService(
            this,
            requestCode,
            Intent(this, GymSessionService::class.java).apply {
                this.action = action
                extras()
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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
