package eric.bitria.minimalfit.data.repository.track

import eric.bitria.minimalfit.data.model.track.Track
import eric.bitria.minimalfit.data.model.track.TrackPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.minutes

/**
 * In-memory implementation of the outdoor activity repository.
 * Uses Flow for reactive updates across ViewModels.
 */
class InMemoryTrackRepository : TrackRepository {

    private val idCounter = AtomicInteger(1)

    // Use StateFlow to emit changes reactively
    private val _activitiesFlow = MutableStateFlow<List<Track>>(emptyList())

    init {
        // Sample route points – a ~5 km loop around Ciutadella Park, Barcelona
        val eveningRunPoints = listOf(
            TrackPoint(41.38879, 2.18992, Instant.parse("2026-03-09T16:30:00Z")),
            TrackPoint(41.38950, 2.19120, Instant.parse("2026-03-09T16:31:00Z")),
            TrackPoint(41.39040, 2.19280, Instant.parse("2026-03-09T16:32:00Z")),
            TrackPoint(41.39150, 2.19450, Instant.parse("2026-03-09T16:33:00Z")),
            TrackPoint(41.39220, 2.19620, Instant.parse("2026-03-09T16:34:00Z")),
            TrackPoint(41.39180, 2.19800, Instant.parse("2026-03-09T16:35:00Z")),
            TrackPoint(41.39090, 2.19950, Instant.parse("2026-03-09T16:36:00Z")),
            TrackPoint(41.38970, 2.20050, Instant.parse("2026-03-09T16:37:00Z")),
            TrackPoint(41.38840, 2.19970, Instant.parse("2026-03-09T16:38:00Z")),
            TrackPoint(41.38720, 2.19820, Instant.parse("2026-03-09T16:39:00Z")),
            TrackPoint(41.38640, 2.19640, Instant.parse("2026-03-09T16:40:00Z")),
            TrackPoint(41.38590, 2.19450, Instant.parse("2026-03-09T16:41:00Z")),
            TrackPoint(41.38610, 2.19260, Instant.parse("2026-03-09T16:42:00Z")),
            TrackPoint(41.38700, 2.19090, Instant.parse("2026-03-09T16:43:00Z")),
            TrackPoint(41.38820, 2.18980, Instant.parse("2026-03-09T16:44:00Z")),
            TrackPoint(41.38879, 2.18992, Instant.parse("2026-03-09T16:45:00Z"))
        )

        // Sample route points – a shorter morning walk near Gràcia, Barcelona
        val morningWalkPoints = listOf(
            TrackPoint(41.40280, 2.15640, Instant.parse("2026-03-08T07:00:00Z")),
            TrackPoint(41.40350, 2.15720, Instant.parse("2026-03-08T07:03:00Z")),
            TrackPoint(41.40440, 2.15810, Instant.parse("2026-03-08T07:06:00Z")),
            TrackPoint(41.40530, 2.15900, Instant.parse("2026-03-08T07:09:00Z")),
            TrackPoint(41.40600, 2.16020, Instant.parse("2026-03-08T07:12:00Z")),
            TrackPoint(41.40660, 2.16180, Instant.parse("2026-03-08T07:15:00Z")),
            TrackPoint(41.40600, 2.16310, Instant.parse("2026-03-08T07:18:00Z")),
            TrackPoint(41.40510, 2.16380, Instant.parse("2026-03-08T07:21:00Z")),
            TrackPoint(41.40410, 2.16300, Instant.parse("2026-03-08T07:24:00Z")),
            TrackPoint(41.40320, 2.16180, Instant.parse("2026-03-08T07:27:00Z")),
            TrackPoint(41.40280, 2.16050, Instant.parse("2026-03-08T07:30:00Z")),
            TrackPoint(41.40280, 2.15640, Instant.parse("2026-03-08T07:45:00Z"))
        )

        _activitiesFlow.value = listOf(
            Track(
                id = idCounter.getAndIncrement().toString(),
                date = LocalDate.now().minusDays(1),
                time = LocalTime.of(18, 30),
                name = "Evening Run",
                distance = 5.2,
                duration = 30.minutes,
                pace = "5:46",
                routePoints = eveningRunPoints
            ),
            Track(
                id = idCounter.getAndIncrement().toString(),
                date = LocalDate.now().minusDays(2),
                time = LocalTime.of(7, 0),
                name = "Morning Walk",
                distance = 3.1,
                duration = 45.minutes,
                pace = "14:31",
                routePoints = morningWalkPoints
            )
        )
    }

    override fun getAllActivitiesFlow(): Flow<List<Track>> = _activitiesFlow

    override fun getAllActivities(): List<Track> = _activitiesFlow.value

    override fun getActivitiesForDate(date: LocalDate): List<Track> =
        _activitiesFlow.value.filter { it.date == date }

    override fun getTrackByIdFlow(id: String): Flow<Track?> =
        _activitiesFlow.map { activities -> activities.find { it.id == id } }

    override fun getTrackById(id: String): Track? =
        _activitiesFlow.value.find { it.id == id }

    override fun addActivity(activity: Track) {
        val newActivity = activity.copy(id = idCounter.getAndIncrement().toString())
        _activitiesFlow.value += newActivity
    }

    override fun updateActivity(activity: Track) {
        _activitiesFlow.value = _activitiesFlow.value.map {
            if (it.id == activity.id) activity else it
        }
    }

    override fun deleteActivity(id: String) {
        _activitiesFlow.value = _activitiesFlow.value.filter { it.id != id }
    }
}
