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
import eric.bitria.minimalfit.data.entity.gym.SessionStatus
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
 *  - ACTION_PAUSE       → pauses the session timer
 *  - ACTION_RESUME      → resumes the session timer
 *  - ACTION_FINISH      → completes the session, removes notification, stops service
 *  - ACTION_START_REST  → starts a rest timer for a given exercise (EXTRA_EXERCISE_ID)
 *  - ACTION_ADD_REST    → adds seconds to the current rest timer (EXTRA_SECONDS)
 *  - ACTION_STOP_REST   → cancels the current rest timer
 *  - ACTION_FINISH_SET  → marks the latest set complete and starts rest
 *  - ACTION_UPDATE_EXERCISE_REST → updates rest duration for an exercise
 *
 * All UI state is observed from the Koin-injected GymTrackingLogic flows,
 * so no binding or LocalBinder is needed.
 */
class GymSessionService : LifecycleService() {

    companion object {
        const val ACTION_START                = "ACTION_START"
        const val ACTION_PAUSE                = "ACTION_PAUSE"
        const val ACTION_RESUME               = "ACTION_RESUME"
        const val ACTION_FINISH               = "ACTION_FINISH"
        const val ACTION_START_REST           = "ACTION_START_REST"
        const val ACTION_ADD_REST             = "ACTION_ADD_REST"
        const val ACTION_STOP_REST            = "ACTION_STOP_REST"
        const val ACTION_FINISH_SET           = "ACTION_FINISH_SET"
        const val ACTION_UPDATE_EXERCISE_REST = "ACTION_UPDATE_EXERCISE_REST"

        const val EXTRA_EXERCISE_ID = "extra_exercise_id"
        const val EXTRA_SECONDS     = "extra_seconds"

        private const val CHANNEL_ID      = "gym_session_channel"
        private const val NOTIFICATION_ID = 2

        // Stable request codes for PendingIntents — must be unique per action
        private const val REQUEST_OPEN           = 0
        private const val REQUEST_FINISH_WORKOUT = 1
        private const val REQUEST_STOP_REST      = 2
        private const val REQUEST_ADD_REST       = 3
        private const val REQUEST_COMPLETE_SET   = 4
    }

    private val trackingLogic: GymTrackingLogic by inject()

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
            ACTION_START  -> startSession()
            ACTION_PAUSE  -> trackingLogic.pause()
            ACTION_RESUME -> trackingLogic.resume()
            ACTION_FINISH -> finishSession()
            ACTION_START_REST -> {
                val exerciseId = intent.getStringExtra(EXTRA_EXERCISE_ID)
                if (!exerciseId.isNullOrBlank()) trackingLogic.startRestForExercise(exerciseId)
            }
            ACTION_ADD_REST -> {
                val seconds = intent.getIntExtra(EXTRA_SECONDS, 30)
                trackingLogic.addRestSeconds(seconds)
            }
            ACTION_STOP_REST  -> trackingLogic.stopRest()
            ACTION_FINISH_SET -> trackingLogic.finishLatestSetAndStartRest()
            ACTION_UPDATE_EXERCISE_REST -> {
                val exerciseId = intent.getStringExtra(EXTRA_EXERCISE_ID)
                val seconds    = intent.getIntExtra(EXTRA_SECONDS, 120)
                if (!exerciseId.isNullOrBlank()) trackingLogic.updateExerciseRest(exerciseId, seconds)
            }
        }
        return START_NOT_STICKY
    }

    /** Not a bound service — binding is unsupported. */
    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    /**
     * If the user swipes the app away while no session is active,
     * there is nothing keeping the service alive, so clean it up.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (!isForeground) stopSelf()
    }

    // -------------------------------------------------------------------------
    // Session control
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // State observation
    // -------------------------------------------------------------------------

    /**
     * Combines all relevant tracking flows so the notification refreshes
     * whenever any of them changes.
     */
    private fun observeTrackingState() {
        lifecycleScope.launch {
            combine(
                trackingLogic.activeSession,
                trackingLogic.elapsed,
                trackingLogic.restRemaining,
                trackingLogic.hasIncompleteSet,
                trackingLogic.nextIncompleteSetInfo,
                trackingLogic.isRestRunning
            ) { values ->
                values[0] as Session?
            }.collect { session ->
                if (isForeground) {
                    updateNotification()
                    if (session == null || session.status == SessionStatus.COMPLETED) {
                        stopSessionService()
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

    // -------------------------------------------------------------------------
    // Notification building
    // -------------------------------------------------------------------------

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
        val activeSession  = trackingLogic.activeSession.value
        val elapsedText    = formatDuration(trackingLogic.elapsed.value)
        val restRunning    = trackingLogic.isRestRunning.value
        val restText       = formatDuration(trackingLogic.restRemaining.value)
        val nextSetInfo    = trackingLogic.nextIncompleteSetInfo.value
        val canCompleteSet = !restRunning &&
                activeSession?.status == SessionStatus.ACTIVE &&
                trackingLogic.hasIncompleteSet.value

        val contentText = if (restRunning) {
            "Rest: $restText  ·  Workout: $elapsedText"
        } else {
            val nextText = nextSetInfo?.let { "  ·  Next: $it" } ?: ""
            "Time: $elapsedText$nextText"
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (restRunning) "Rest timer" else "Workout in progress")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(buildOpenAppIntent())
            .setOnlyAlertOnce(true)
            .setOngoing(true)

        if (restRunning) {
            builder.addAction(0, "+30s", buildServiceIntent(ACTION_ADD_REST, REQUEST_ADD_REST) {
                putExtra(EXTRA_SECONDS, 30)
            })
            builder.addAction(0, "Stop Rest", buildServiceIntent(ACTION_STOP_REST, REQUEST_STOP_REST))
        } else if (canCompleteSet) {
            builder.addAction(0, "Complete Set", buildServiceIntent(ACTION_FINISH_SET, REQUEST_COMPLETE_SET))
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