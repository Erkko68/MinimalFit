package eric.bitria.minimalfit.data.gym

import android.content.Context
import android.content.Intent
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.service.GymSessionService

class AndroidGymSessionManager(
    private val context: Context,
    private val gymTrackingLogic: GymTrackingLogic
) : GymSessionManager {

    override val activeSets = gymTrackingLogic.activeSets
    override val elapsed = gymTrackingLogic.elapsed
    override val restRemaining = gymTrackingLogic.restRemaining
    override val isRestRunning = gymTrackingLogic.isRestRunning

    override fun start() {
        sendCommand(GymSessionService.ACTION_START)
    }

    override fun finish() {
        sendCommand(GymSessionService.ACTION_FINISH)
    }

    override fun addSet(exerciseId: String) {
        gymTrackingLogic.addSet(exerciseId)
    }

    override fun updateSet(set: Set) {
        gymTrackingLogic.updateSet(set)
    }

    override fun deleteSet(setId: String) {
        gymTrackingLogic.deleteSet(setId)
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

    private fun sendCommand(action: String) {
        val intent = Intent(context, GymSessionService::class.java).apply {
            this.action = action
        }
        context.startService(intent)
    }
}
