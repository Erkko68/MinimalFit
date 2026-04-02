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
import eric.bitria.minimalfit.data.entity.gym.SessionStatus
import eric.bitria.minimalfit.data.gym.GymTrackingLogic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.time.Duration

class GymSessionService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_FINISH = "ACTION_FINISH"
        const val ACTION_START_REST = "ACTION_START_REST"
        const val ACTION_ADD_REST = "ACTION_ADD_REST"
        const val ACTION_FINISH_SET = "ACTION_FINISH_SET"
        const val ACTION_UPDATE_EXERCISE_REST = "ACTION_UPDATE_EXERCISE_REST"

        const val EXTRA_EXERCISE_ID = "extra_exercise_id"
        const val EXTRA_SECONDS = "extra_seconds"

        private const val CHANNEL_ID = "gym_session_channel"
        private const val NOTIFICATION_ID = 2
    }

    private val trackingLogic: GymTrackingLogic by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val binder = LocalBinder()
    private var isBound = false
    private var isForegroundService = false

    inner class LocalBinder : Binder() {
        fun getService(): GymSessionService = this@GymSessionService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        serviceScope.launch {
            launch {
                trackingLogic.activeSession.collect { session ->
                    if (session == null || session.status == SessionStatus.COMPLETED) {
                        stopSessionService()
                        return@collect
                    }
                    if (isForegroundService) updateNotification()
                }
            }
            launch {
                trackingLogic.elapsed.collect {
                    if (isForegroundService) updateNotification()
                }
            }
            launch {
                trackingLogic.restRemaining.collect {
                    if (isForegroundService) updateNotification()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startSession()
            ACTION_PAUSE -> trackingLogic.pause()
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
            ACTION_FINISH_SET -> trackingLogic.finishLatestSetAndStartRest()
            ACTION_UPDATE_EXERCISE_REST -> {
                val exerciseId = intent.getStringExtra(EXTRA_EXERCISE_ID)
                val seconds = intent.getIntExtra(EXTRA_SECONDS, 120)
                if (!exerciseId.isNullOrBlank()) {
                    trackingLogic.updateExerciseRest(exerciseId, seconds)
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        isBound = true
        demoteToBackground()
        return binder
    }

    override fun onRebind(intent: Intent?) {
        isBound = true
        demoteToBackground()
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isBound = false
        val activeSession = trackingLogic.activeSession.value
        if (activeSession != null && activeSession.status == SessionStatus.ACTIVE) {
            promoteToForeground()
        }
        return true
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startSession() {
        trackingLogic.start()
        if (!isBound) {
            promoteToForeground()
        }
    }

    private fun finishSession() {
        trackingLogic.finish()
        stopSessionService()
    }

    private fun stopSessionService() {
        demoteToBackground()
        stopSelf()
    }

    private fun promoteToForeground() {
        if (isForegroundService) return
        isForegroundService = true
        startForeground(
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
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
            "Gym Session",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Shows your workout timer while the app is in background"
        }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val finishWorkoutIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, GymSessionService::class.java).apply { action = ACTION_FINISH },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val finishSetIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, GymSessionService::class.java).apply { action = ACTION_FINISH_SET },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val addRestIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, GymSessionService::class.java).apply {
                action = ACTION_ADD_REST
                putExtra(EXTRA_SECONDS, 30)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val elapsedText = formatDuration(trackingLogic.elapsed.value)
        val restRunning = trackingLogic.isRestRunning.value
        val restText = formatDuration(trackingLogic.restRemaining.value)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (restRunning) "Rest timer" else "Workout in progress")
            .setContentText(if (restRunning) "Rest: $restText | Workout: $elapsedText" else "Time: $elapsedText")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)

        if (restRunning) {
            builder.addAction(0, "+30s", addRestIntent)
        }

        builder.addAction(0, "Finish Set", finishSetIntent)
        builder.addAction(0, "Finish Workout", finishWorkoutIntent)

        return builder.build()
    }

    private fun formatDuration(duration: Duration): String {
        val totalSeconds = duration.inWholeSeconds
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }
}

