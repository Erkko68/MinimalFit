package eric.bitria.minimalfit.data.track

import eric.bitria.minimalfit.data.model.track.TrackPoint
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

/**
 * Common interface for tracking.
 * ViewModels will interact only with this.
 */
interface TrackingManager {
    val recordingState: StateFlow<RecordingState>
    val routePoints: StateFlow<List<TrackPoint>>
    val distanceKm: StateFlow<Double>
    val duration: StateFlow<Duration>
    val pace: StateFlow<String>

    fun start()
    fun pause()
    fun resume()
    fun stop()
}
