package eric.bitria.minimalfit.data.datasource

import eric.bitria.minimalfit.data.model.track.Track
import eric.bitria.minimalfit.data.model.track.TrackPoint
import eric.bitria.minimalfit.util.today
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import java.util.UUID
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * In-memory database for tracking activities.
 */
class TrackDatabase {
    private val eveningRunPoints = listOf(
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

    private val morningWalkPoints = listOf(
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

    private val _activities = MutableStateFlow(listOf(
        Track(
            id = "1",
            date = today().minus(1, DateTimeUnit.DAY),
            time = LocalTime(18, 30), // if using kotlinx.datetime.LocalTime
            name = "Evening Run",
            distance = 5.2,
            duration = 30.minutes,
            pace = "5:46",
            routePoints = eveningRunPoints
        ),
        Track(
            id = "2",
            date = today().minus(2, DateTimeUnit.DAY),
            time = LocalTime(7, 0),
            name = "Morning Walk",
            distance = 3.1,
            duration = 45.minutes,
            pace = "14:31",
            routePoints = morningWalkPoints
        )
    ))

    fun getTracks(query: String, limit: Int): Flow<List<Track>> {
        return _activities.map { activities ->
            val filtered = if (query.isBlank()) activities
            else activities.filter { it.name.contains(query, ignoreCase = true) }

            filtered.sortedByDescending { it.date }.take(limit)
        }
    }

    fun getTracks(start: LocalDate, end: LocalDate): Flow<List<Track>> {
        return _activities.map { activities ->
            activities.filter { it.date in start..end } // use comparison operators
                .sortedByDescending { it.date } // sort by date descending
        }
    }

    fun getTrack(id: String): Flow<Track?> =
        _activities.map { activities -> activities.find { it.id == id } }

    suspend fun addTrack(track: Track) {
        val newTrack = if (track.id.isBlank()) track.copy(id = UUID.randomUUID().toString()) else track
        _activities.value += newTrack
    }

    suspend fun updateTrack(track: Track) {
        _activities.value = _activities.value.map {
            if (it.id == track.id) track else it
        }
    }

    suspend fun deleteTrack(id: String) {
        _activities.value = _activities.value.filter { it.id != id }
    }
}
