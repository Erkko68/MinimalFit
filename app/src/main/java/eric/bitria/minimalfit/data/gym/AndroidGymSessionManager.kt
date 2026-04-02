package eric.bitria.minimalfit.data.gym

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import eric.bitria.minimalfit.service.GymSessionService

class AndroidGymSessionManager(
    private val context: Context,
    private val gymTrackingLogic: GymTrackingLogic
) : GymSessionManager {

    override val activeSession = gymTrackingLogic.activeSession
    override val elapsed = gymTrackingLogic.elapsed

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

    private fun sendCommand(action: String) {
        val intent = Intent(context, GymSessionService::class.java).apply {
            this.action = action
        }
        ContextCompat.startForegroundService(context, intent)
    }
}

