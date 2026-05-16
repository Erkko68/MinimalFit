package eric.bitria.minimalfit.data.gym

import android.content.Context
import android.content.Intent
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.service.GymSessionService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AndroidGymSessionManager(
    private val context: Context,
    private val gymTrackingLogic: GymTrackingLogic
) : GymSessionManager {

    override val activeSessionExercises = gymTrackingLogic.activeSessionExercises
    override val activeSets = gymTrackingLogic.activeSets
    override val elapsed = gymTrackingLogic.elapsed
    override val restRemaining = gymTrackingLogic.restRemaining
    override val isRestRunning = gymTrackingLogic.isRestRunning
    override val isPaused = gymTrackingLogic.isPaused
    override val activeSession = gymTrackingLogic.activeSession

    override fun start() {
        sendCommand(GymSessionService.ACTION_START)
    }

    override fun startFromRoutine(exercises: List<RoutineExercisePlan>, routineName: String) {
        sendRoutineCommand(
            action = GymSessionService.ACTION_START_ROUTINE,
            exercises = exercises,
            routineName = routineName
        )
    }

    override fun replaceWithRoutine(exercises: List<RoutineExercisePlan>, routineName: String) {
        sendRoutineCommand(
            action = GymSessionService.ACTION_REPLACE_WITH_ROUTINE,
            exercises = exercises,
            routineName = routineName
        )
    }

    private fun sendRoutineCommand(
        action: String,
        exercises: List<RoutineExercisePlan>,
        routineName: String
    ) {
        val intent = Intent(context, GymSessionService::class.java).apply {
            this.action = action
            putExtra(GymSessionService.EXTRA_ROUTINE_PLANS_JSON, Json.encodeToString(exercises))
            putStringArrayListExtra(
                GymSessionService.EXTRA_EXERCISE_IDS,
                ArrayList(exercises.map { it.exerciseId })
            )
            putIntegerArrayListExtra(
                GymSessionService.EXTRA_TARGET_SETS,
                ArrayList(exercises.map { it.targetSets })
            )
            putIntegerArrayListExtra(
                GymSessionService.EXTRA_TARGET_REPS,
                ArrayList(exercises.map { it.targetReps })
            )
            putStringArrayListExtra(
                GymSessionService.EXTRA_TARGET_TYPES,
                ArrayList(exercises.map { it.targetType })
            )
            putIntegerArrayListExtra(
                GymSessionService.EXTRA_TARGET_DURATIONS,
                ArrayList(exercises.map { it.targetDurationSeconds })
            )
            putIntegerArrayListExtra(
                GymSessionService.EXTRA_PREPARATION_SECONDS,
                ArrayList(exercises.map { it.targetPreparationSeconds })
            )
            putExtra(
                GymSessionService.EXTRA_TARGET_WEIGHTS,
                exercises.map { it.targetWeight }.toFloatArray()
            )
            putExtra(GymSessionService.EXTRA_SESSION_TITLE, routineName)
        }
        context.startService(intent)
    }

    override fun loadSession(sessionId: String) {
        gymTrackingLogic.loadSession(sessionId)
    }

    override fun finish() {
        sendCommand(GymSessionService.ACTION_FINISH)
    }

    override fun pause() {
        gymTrackingLogic.pause()
    }

    override fun resume() {
        sendCommand(GymSessionService.ACTION_RESUME)
    }

    override fun addExercise(exerciseId: String) {
        gymTrackingLogic.addExercise(exerciseId)
    }

    override fun addSet(sessionExerciseId: String, weight: Float, reps: Int, isCompleted: Boolean) {
        gymTrackingLogic.addSet(sessionExerciseId, weight, reps, isCompleted)
    }

    override fun addTimedSet(
        sessionExerciseId: String,
        weight: Float,
        durationSeconds: Int,
        preparationSeconds: Int,
        isCompleted: Boolean
    ) {
        gymTrackingLogic.addTimedSet(
            sessionExerciseId = sessionExerciseId,
            weight = weight,
            durationSeconds = durationSeconds,
            preparationSeconds = preparationSeconds,
            isCompleted = isCompleted
        )
    }

    override fun updateSet(set: Set) {
        gymTrackingLogic.updateSet(set)
    }

    override fun deleteSet(setId: String) {
        gymTrackingLogic.deleteSet(setId)
    }

    override fun deleteExercise(sessionExerciseId: String) {
        gymTrackingLogic.deleteExercise(sessionExerciseId)
    }

    override fun startRest(seconds: Int) {
        val intent = Intent(context, GymSessionService::class.java).apply {
            action = GymSessionService.ACTION_START_REST
            putExtra(GymSessionService.EXTRA_SECONDS, seconds)
        }
        context.startService(intent)
    }

    override fun stopRest() {
        sendCommand(GymSessionService.ACTION_STOP_REST)
    }

    override fun updateSessionTitle(title: String) {
        gymTrackingLogic.updateSessionTitle(title)
    }

    private fun sendCommand(action: String) {
        val intent = Intent(context, GymSessionService::class.java).apply {
            this.action = action
        }
        context.startService(intent)
    }
}
