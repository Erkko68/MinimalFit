package eric.bitria.minimalfit.data.track

import eric.bitria.minimalfit.data.model.track.Track
import eric.bitria.minimalfit.data.model.track.TrackPoint
import eric.bitria.minimalfit.data.repository.track.LocationRepository
import eric.bitria.minimalfit.data.repository.track.TrackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
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

/**
 * Shared tracking logic. This will eventually move to commonMain in KMP.
 */
class TrackingLogic(
    private val locationRepository: LocationRepository,
    private val trackRepository: TrackRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _routePoints = MutableStateFlow<List<TrackPoint>>(emptyList())
    val routePoints: StateFlow<List<TrackPoint>> = _routePoints.asStateFlow()

    private val _distanceKm = MutableStateFlow(0.0)
    val distanceKm: StateFlow<Double> = _distanceKm.asStateFlow()

    private val _duration = MutableStateFlow(Duration.ZERO)
    val duration: StateFlow<Duration> = _duration.asStateFlow()

    private val _pace = MutableStateFlow("--:--")
    val pace: StateFlow<String> = _pace.asStateFlow()

    private var timerJob: Job? = null
    private var locationCollectJob: Job? = null
    private var elapsedSeconds = 0L

    fun start() {
        if (_recordingState.value == RecordingState.RECORDING) return
        
        locationRepository.startTracking()
        _recordingState.value = RecordingState.RECORDING
        startTimer()
        startCollectingPoints()
    }

    fun pause() {
        if (_recordingState.value != RecordingState.RECORDING) return
        
        _recordingState.value = RecordingState.PAUSED
        stopInternal()
    }

    fun stop() {
        stopInternal()
        if (_routePoints.value.isNotEmpty()) {
            saveTrack()
        }
        reset()
    }

    private fun stopInternal() {
        timerJob?.cancel()
        locationCollectJob?.cancel()
        locationRepository.stopTracking()
    }

    private fun reset() {
        _recordingState.value = RecordingState.IDLE
        _routePoints.value = emptyList()
        _distanceKm.value = 0.0
        _duration.value = Duration.ZERO
        _pace.value = "--:--"
        elapsedSeconds = 0L
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (true) {
                delay(1000)
                elapsedSeconds++
                _duration.value = elapsedSeconds.seconds
            }
        }
    }

    private fun startCollectingPoints() {
        locationCollectJob?.cancel()
        locationCollectJob = scope.launch {
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
}
