package eric.bitria.minimalfit.data.track

import android.content.Context
import android.content.Intent
import eric.bitria.minimalfit.service.LocationService

/**
 * Android implementation of TrackingManager.
 * It communicates with the LocationService via Intents.
 */
class AndroidTrackingManager(
    private val context: Context,
    private val trackingLogic: TrackingLogic
) : TrackingManager {

    override val recordingState = trackingLogic.recordingState
    override val routePoints = trackingLogic.routePoints
    override val distanceKm = trackingLogic.distanceKm
    override val duration = trackingLogic.duration
    override val pace = trackingLogic.pace

    override fun start() {
        sendCommand(LocationService.ACTION_START)
    }

    override fun pause() {
        sendCommand(LocationService.ACTION_PAUSE)
    }

    override fun resume() {
        sendCommand(LocationService.ACTION_RESUME)
    }

    override fun stop() {
        sendCommand(LocationService.ACTION_STOP)
    }

    private fun sendCommand(action: String) {
        val intent = Intent(context, LocationService::class.java).apply {
            this.action = action
        }
        context.startService(intent)
    }
}
