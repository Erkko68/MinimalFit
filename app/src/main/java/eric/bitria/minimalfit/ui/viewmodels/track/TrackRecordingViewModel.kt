package eric.bitria.minimalfit.ui.viewmodels.track

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.track.TrackPoint
import eric.bitria.minimalfit.data.repository.track.LocationRepository
import eric.bitria.minimalfit.data.track.RecordingState
import eric.bitria.minimalfit.data.track.TrackingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration

data class TrackRecordingUiState(
    val recordingState: RecordingState = RecordingState.IDLE,
    val routePoints: List<TrackPoint> = emptyList(),
    val distanceKm: Double = 0.0,
    val duration: Duration = Duration.ZERO,
    val pace: String = "0:00",
    val currentLocation: Location? = null
)

class TrackRecordingViewModel(
    private val locationRepository: LocationRepository,
    private val trackingManager: TrackingManager
) : ViewModel() {

    private val _currentLocation = MutableStateFlow<Location?>(null)

    val uiState: StateFlow<TrackRecordingUiState> = combine(
        trackingManager.recordingState,
        trackingManager.routePoints,
        trackingManager.distanceKm,
        trackingManager.duration,
        trackingManager.pace,
        _currentLocation
    ) { args: Array<Any?> ->
        TrackRecordingUiState(
            recordingState = args[0] as RecordingState,
            routePoints = args[1] as List<TrackPoint>,
            distanceKm = args[2] as Double,
            duration = args[3] as Duration,
            pace = args[4] as String,
            currentLocation = args[5] as (Location?)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TrackRecordingUiState()
    )

    init {
        // Passively collect locations to update the map marker when not recording
        viewModelScope.launch {
            locationRepository.location.filterNotNull().collect { location ->
                val isFirstFetch = _currentLocation.value == null && uiState.value.recordingState == RecordingState.IDLE
                _currentLocation.value = location

                // Stop GPS after first fetch if idle to save battery
                if (isFirstFetch) {
                    locationRepository.stopTracking()
                }
            }
        }
    }

    fun requestInitialLocation() {
        if (_currentLocation.value == null && uiState.value.recordingState == RecordingState.IDLE) {
            locationRepository.startTracking()
        }
    }

    fun startOrResume() {
        if (uiState.value.recordingState == RecordingState.PAUSED) {
            trackingManager.resume()
        } else {
            trackingManager.start()
        }
    }

    fun pause() = trackingManager.pause()
    fun stop() = trackingManager.stop()
}
