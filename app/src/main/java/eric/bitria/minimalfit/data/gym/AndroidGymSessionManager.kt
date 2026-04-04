package eric.bitria.minimalfit.data.gym

import android.content.Context
import android.content.Intent
import eric.bitria.minimalfit.service.GymSessionService

class AndroidGymSessionManager(
    private val context: Context,
    private val gymTrackingLogic: GymTrackingLogic
) : GymSessionManager {

    override val activeSession = gymTrackingLogic.activeSession
    override val elapsed = gymTrackingLogic.elapsed
    override val restRemaining = gymTrackingLogic.restRemaining
    override val isRestRunning = gymTrackingLogic.isRestRunning

    override fun start() {
        sendCommand(GymSessionService.ACTION_START)
    }

    override fun pause() {
        sendCommand(GymSessionService.ACTION_PAUSE)
    }

    override fun resume() {
        sendCommand(GymSessionService.ACTION_RESUME)
    }

    override fun finish() {
        sendCommand(GymSessionService.ACTION_FINISH)
    }

    override fun startRestForExercise(exerciseId: String) {
        val intent = Intent(context, GymSessionService::class.java).apply {
            action = GymSessionService.ACTION_START_REST
            putExtra(GymSessionService.EXTRA_EXERCISE_ID, exerciseId)
        }
        context.startService(intent)
    }

    override fun addRestSeconds(seconds: Int) {
        val intent = Intent(context, GymSessionService::class.java).apply {
            action = GymSessionService.ACTION_ADD_REST
            putExtra(GymSessionService.EXTRA_SECONDS, seconds)
        }
        context.startService(intent)
    }

    override fun stopRest() {
        sendCommand(GymSessionService.ACTION_STOP_REST)
    }

    override fun finishLatestSetAndStartRest() {
        sendCommand(GymSessionService.ACTION_FINISH_SET)
    }

    override fun updateExerciseRest(exerciseId: String, restSeconds: Int) {
        val intent = Intent(context, GymSessionService::class.java).apply {
            action = GymSessionService.ACTION_UPDATE_EXERCISE_REST
            putExtra(GymSessionService.EXTRA_EXERCISE_ID, exerciseId)
            putExtra(GymSessionService.EXTRA_SECONDS, restSeconds)
        }
        context.startService(intent)
    }

    private fun sendCommand(action: String) {
        val intent = Intent(context, GymSessionService::class.java).apply {
            this.action = action
        }
        context.startService(intent)
    }
}

