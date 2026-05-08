package eric.bitria.minimalfit.data.gym

import android.content.Context
import android.content.Intent
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.service.GymSessionService

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
        gymTrackingLogic.resume()
    }

    override fun addExercise(exerciseId: String) {
        gymTrackingLogic.addExercise(exerciseId)
    }

    override fun addSet(sessionExerciseId: String) {
        gymTrackingLogic.addSet(sessionExerciseId)
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
