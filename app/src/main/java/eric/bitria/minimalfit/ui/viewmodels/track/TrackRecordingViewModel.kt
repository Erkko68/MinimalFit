package eric.bitria.minimalfit.ui.viewmodels.track

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.model.Track
import eric.bitria.minimalfit.data.model.TrackPoint
import eric.bitria.minimalfit.data.repository.track.LocationRepository
import eric.bitria.minimalfit.data.repository.track.TrackRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

enum class RecordingState { IDLE, RECORDING, PAUSED }

class TrackRecordingViewModel(
    private val locationRepository: LocationRepository,
    private val trackRepository: TrackRepository
) : ViewModel() {

    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _routePoints = MutableStateFlow<List<TrackPoint>>(emptyList())
    val routePoints: StateFlow<List<TrackPoint>> = _routePoints.asStateFlow()

    private val _distanceKm = MutableStateFlow(0.0)
    val distanceKm: StateFlow<Double> = _distanceKm.asStateFlow()

    private val _duration = MutableStateFlow(Duration.ZERO)
    val duration: StateFlow<Duration> = _duration.asStateFlow()

    private val _pace = MutableStateFlow("--:--")
    val pace: StateFlow<String> = _pace.asStateFlow()

    val isGpsEnabled: StateFlow<Boolean> = locationRepository.isGpsEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    private val _savedTrackId = MutableStateFlow<String?>(null)
    val savedTrackId: StateFlow<String?> = _savedTrackId.asStateFlow()

    private var timerJob: Job? = null
    private var locationCollectJob: Job? = null
    private var elapsedSeconds = 0L

    init {
        // Passively collect locations to update the map marker
        viewModelScope.launch {
            locationRepository.location.filterNotNull().collect { location ->
                val isFirstFetch = _currentLocation.value == null && _recordingState.value == RecordingState.IDLE

                _currentLocation.value = location

                // If this was our very first preview location, and we aren't recording yet,
                // shut down the GPS stream to save battery until the user hits "Start".
                if (isFirstFetch) {
                    locationRepository.stopTracking()
                }
            }
        }
    }

    // Called by the UI once permissions are safely granted
    fun requestInitialLocation() {
        if (_currentLocation.value == null && _recordingState.value == RecordingState.IDLE) {
            locationRepository.startTracking()
        }
    }

    fun startOrResume() {
        locationRepository.startTracking()
        _recordingState.value = RecordingState.RECORDING
        startTimer()
        startCollectingPoints()
    }

    fun pause() {
        _recordingState.value = RecordingState.PAUSED
        timerJob?.cancel()
        locationCollectJob?.cancel()
    }

    fun stop() {
        timerJob?.cancel()
        locationCollectJob?.cancel()
        locationRepository.stopTracking()

        if (_routePoints.value.isNotEmpty()) {
            saveTrack()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                elapsedSeconds++
                _duration.value = elapsedSeconds.seconds
            }
        }
    }

    private fun startCollectingPoints() {
        locationCollectJob?.cancel()
        locationCollectJob = viewModelScope.launch {
            locationRepository.location
                .filterNotNull()
                .distinctUntilChanged { old, new ->
                    old.latitude == new.latitude && old.longitude == new.longitude
                }
                .collect { location ->
                    val newPoint = TrackPoint(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = Instant.now()
                    )
                    val updatedPoints = _routePoints.value + newPoint
                    val newDistance = calculateTotalDistanceKm(updatedPoints)
                    _routePoints.value = updatedPoints
                    _distanceKm.value = newDistance
                    _pace.value = calculatePace(newDistance, elapsedSeconds)
                }
        }
    }

    private fun saveTrack() {
        val now = LocalDate.now()
        val time = LocalTime.now()
        val id = UUID.randomUUID().toString()
        val track = Track(
            id = id,
            date = now,
            time = time,
            name = "Run on $now",
            distance = _distanceKm.value,
            duration = _duration.value,
            pace = _pace.value,
            routePoints = _routePoints.value
        )
        trackRepository.addActivity(track)
        _savedTrackId.value = id
        _recordingState.value = RecordingState.IDLE
    }

    private fun calculateTotalDistanceKm(points: List<TrackPoint>): Double {
        if (points.size < 2) return 0.0
        var total = 0.0
        for (i in 1 until points.size) {
            total += haversineKm(points[i - 1], points[i])
        }
        return total
    }

    private fun haversineKm(a: TrackPoint, b: TrackPoint): Double {
        val r = 6371.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val x = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        return 2 * r * atan2(sqrt(x), sqrt(1 - x))
    }

    private fun calculatePace(distanceKm: Double, elapsedSeconds: Long): String {
        if (distanceKm < 0.001) return "--:--"
        val secondsPerKm = elapsedSeconds / distanceKm
        val minutes = (secondsPerKm / 60).toLong()
        val seconds = (secondsPerKm % 60).toLong()
        return "%d:%02d".format(minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        locationCollectJob?.cancel()
        locationRepository.stopTracking()
    }
}