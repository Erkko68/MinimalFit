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
import eric.bitria.minimalfit.data.entity.gym.Set as GymSet
import eric.bitria.minimalfit.data.gym.GymTrackingLogic
import eric.bitria.minimalfit.data.gym.RoutineExercisePlan
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.time.Duration

/**
 * GymSessionService manages gym workout tracking as a foreground service.
 */
class GymSessionService : LifecycleService() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_START_ROUTINE = "ACTION_START_ROUTINE"
        const val ACTION_REPLACE_WITH_ROUTINE = "ACTION_REPLACE_WITH_ROUTINE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_FINISH = "ACTION_FINISH"
        const val ACTION_START_REST = "ACTION_START_REST"
        const val ACTION_STOP_REST = "ACTION_STOP_REST"

        const val EXTRA_EXERCISE_IDS = "extra_exercise_ids"
        const val EXTRA_TARGET_SETS = "extra_target_sets"
        const val EXTRA_TARGET_REPS = "extra_target_reps"
        const val EXTRA_TARGET_WEIGHTS = "extra_target_weights"
        const val EXTRA_SESSION_TITLE = "extra_session_title"
        const val EXTRA_SECONDS = "extra_seconds"

        private const val CHANNEL_ID = "gym_session_channel"
        private const val NOTIFICATION_ID = 2

        private const val REQUEST_OPEN = 0
        private const val REQUEST_FINISH_WORKOUT = 1
        private const val REQUEST_STOP_REST = 2
        private const val REQUEST_START_REST = 3
    }

    private val trackingLogic: GymTrackingLogic by inject()
    private val exerciseRepository: ExerciseRepository by inject()

    private var isForeground = false
    private var exerciseNamesById: Map<String, String> = emptyMap()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        observeExerciseNames()
        observeTrackingState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> startSession()
            ACTION_START_ROUTINE -> {
                val exercises = intent.toRoutineExercisePlans()
                val routineName = intent.getStringExtra(EXTRA_SESSION_TITLE).orEmpty()
                startSessionFromRoutine(exercises, routineName)
            }
            ACTION_REPLACE_WITH_ROUTINE -> {
                val exercises = intent.toRoutineExercisePlans()
                val routineName = intent.getStringExtra(EXTRA_SESSION_TITLE).orEmpty()
                replaceSessionWithRoutine(exercises, routineName)
            }
            ACTION_RESUME -> resumeSession()
            ACTION_FINISH -> finishSession()
            ACTION_START_REST -> {
                val seconds = intent.getIntExtra(EXTRA_SECONDS, 60)
                trackingLogic.startRest(seconds)
            }
            ACTION_STOP_REST -> trackingLogic.stopRest()
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

    private fun startSessionFromRoutine(exercises: List<RoutineExercisePlan>, routineName: String) {
        trackingLogic.startFromRoutine(exercises, routineName)
        showForegroundNotification()
    }

    private fun replaceSessionWithRoutine(exercises: List<RoutineExercisePlan>, routineName: String) {
        trackingLogic.replaceWithRoutine(exercises, routineName)
        showForegroundNotification()
    }

    private fun Intent.toRoutineExercisePlans(): List<RoutineExercisePlan> {
        val exerciseIds = getStringArrayListExtra(EXTRA_EXERCISE_IDS).orEmpty()
        val targetSets = getIntegerArrayListExtra(EXTRA_TARGET_SETS).orEmpty()
        val targetReps = getIntegerArrayListExtra(EXTRA_TARGET_REPS).orEmpty()
        val targetWeights = getFloatArrayExtra(EXTRA_TARGET_WEIGHTS) ?: FloatArray(0)
        return exerciseIds.mapIndexed { index, exerciseId ->
            RoutineExercisePlan(
                exerciseId = exerciseId,
                targetSets = targetSets.getOrNull(index) ?: 1,
                targetReps = targetReps.getOrNull(index) ?: 0,
                targetWeight = targetWeights.getOrNull(index) ?: 0f
            )
        }
    }

    private fun resumeSession() {
        trackingLogic.resume()
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
                trackingLogic.isRestRunning,
                trackingLogic.activeSessionExercises,
                trackingLogic.activeSets
            ) { args: Array<Any?> ->
                args[0]
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

    private fun observeExerciseNames() {
        lifecycleScope.launch {
            exerciseRepository.getExercises(limit = -1).collect { exercises ->
                exerciseNamesById = exercises.associate { it.id to it.name }
                if (isForeground) updateNotification()
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
        val session = trackingLogic.activeSession.value
        val elapsedText = formatDuration(trackingLogic.elapsed.value)
        val restRunning = trackingLogic.isRestRunning.value
        val restText = formatDuration(trackingLogic.restRemaining.value)
        val stats = buildWorkoutStats()
        val setProgress = if (stats.totalSets > 0) {
            "${stats.completedSets}/${stats.totalSets} sets"
        } else {
            "No sets yet"
        }
        val exerciseContext = stats.currentExerciseName.takeIf { it != "-" }
        val contentText = when {
            restRunning && exerciseContext != null -> "Rest $restText - $exerciseContext - $setProgress"
            restRunning -> "Rest $restText - $setProgress"
            trackingLogic.isPaused.value && exerciseContext != null -> "Paused - $exerciseContext - $setProgress"
            trackingLogic.isPaused.value -> "Paused - $setProgress"
            exerciseContext != null -> "$exerciseContext - $elapsedText - $setProgress"
            else -> "$elapsedText - $setProgress"
        }
        val title = session?.title?.takeIf { it.isNotBlank() } ?: "Workout in progress"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (restRunning) "Rest timer" else title)
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    buildExpandedNotificationText(
                        elapsedText = elapsedText,
                        restText = restText,
                        restRunning = restRunning,
                        stats = stats
                    )
                )
            )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(buildOpenAppIntent())
            .setSubText("${stats.exerciseCount} exercises")
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

    private data class WorkoutStats(
        val exerciseCount: Int,
        val completedSets: Int,
        val totalSets: Int,
        val currentExerciseName: String,
        val nextSet: GymSet?
    )

    private fun buildWorkoutStats(): WorkoutStats {
        val sets = trackingLogic.activeSets.value.sortedBy { it.createdAt }
        val sessionExercises = trackingLogic.activeSessionExercises.value
        val currentSessionExercise = sets
            .firstOrNull { !it.isCompleted }
            ?.let { nextSet -> sessionExercises.firstOrNull { it.id == nextSet.sessionExerciseId } }
            ?: sessionExercises.lastOrNull()
        val currentExerciseName = currentSessionExercise
            ?.let { exerciseNamesById[it.exerciseId] }
            ?: "-"
        return WorkoutStats(
            exerciseCount = sessionExercises.size,
            completedSets = sets.count { it.isCompleted },
            totalSets = sets.size,
            currentExerciseName = currentExerciseName,
            nextSet = sets.firstOrNull { !it.isCompleted }
        )
    }

    private fun buildExpandedNotificationText(
        elapsedText: String,
        restText: String,
        restRunning: Boolean,
        stats: WorkoutStats
    ): String = buildString {
        appendLine("Workout time: $elapsedText")
        if (restRunning) appendLine("Rest remaining: $restText")
        appendLine("Current: ${stats.currentExerciseName}")
        appendLine("Exercises: ${stats.exerciseCount}")
        appendLine("Sets completed: ${stats.completedSets}/${stats.totalSets}")
        stats.nextSet?.let { set ->
            append("Next set: ${formatSet(set)}")
        }
    }.trim()

    private fun formatSet(set: GymSet): String {
        val weightText = if (set.weight == 0f) {
            "bodyweight"
        } else {
            "%g kg".format(set.weight)
        }
        return "$weightText x ${set.reps}"
    }

    private fun buildOpenAppIntent(): PendingIntent =
        PendingIntent.getActivity(
            this,
            REQUEST_OPEN,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_OPEN_GYM_SESSION, true)
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
