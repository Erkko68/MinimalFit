package eric.bitria.minimalfit.ui.viewmodels.track

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.repository.track.LocationRepository
import eric.bitria.minimalfit.data.track.RecordingState
import eric.bitria.minimalfit.data.track.TrackingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrackRecordingViewModel(
    private val locationRepository: LocationRepository,
    private val trackingManager: TrackingManager
) : ViewModel() {

    val recordingState = trackingManager.recordingState
    val routePoints = trackingManager.routePoints
    val distanceKm = trackingManager.distanceKm
    val duration = trackingManager.duration
    val pace = trackingManager.pace

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    val isGpsEnabled: StateFlow<Boolean> = locationRepository.isGpsEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    init {
        // Passively collect locations to update the map marker when not recording
        viewModelScope.launch {
            locationRepository.location.filterNotNull().collect { location ->
                val isFirstFetch = _currentLocation.value == null && recordingState.value == RecordingState.IDLE
                _currentLocation.value = location

                // Stop GPS after first fetch if idle to save battery
                if (isFirstFetch) {
                    locationRepository.stopTracking()
                }
            }
        }
    }

    fun requestInitialLocation() {
        if (_currentLocation.value == null && recordingState.value == RecordingState.IDLE) {
            locationRepository.startTracking()
        }
    }

    fun startOrResume() {
        if (recordingState.value == RecordingState.PAUSED) {
            trackingManager.resume()
        } else {
            trackingManager.start()
        }
    }

    fun pause() = trackingManager.pause()
    fun stop() = trackingManager.stop()
}
