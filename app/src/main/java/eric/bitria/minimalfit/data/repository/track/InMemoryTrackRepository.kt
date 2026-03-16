package eric.bitria.minimalfit.data.repository.track

import eric.bitria.minimalfit.data.datasource.TrackDatabase
import eric.bitria.minimalfit.data.model.track.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID

/**
 * In-memory implementation of the outdoor activity repository.
 */
class InMemoryTrackRepository(private val trackDatabase: TrackDatabase) : TrackRepository {

    private val _activities = MutableStateFlow(trackDatabase.initialTracks)

    override fun getTracks(query: String, limit: Int): Flow<List<Track>> {
        return _activities.map { activities ->
            val filtered = if (query.isBlank()) activities
            else activities.filter { it.name.contains(query, ignoreCase = true) }
            
            filtered.sortedByDescending { it.date }.take(limit)
        }
    }

    override fun getTracks(start: LocalDate, end: LocalDate): Flow<List<Track>> {
        return _activities.map { activities ->
            activities.filter { !it.date.isBefore(start) && !it.date.isAfter(end) }
                .sortedByDescending { it.date }
        }
    }

    override fun getTrack(id: String): Flow<Track?> =
        _activities.map { activities -> activities.find { it.id == id } }

    override suspend fun addTrack(track: Track) {
        val newTrack = if (track.id.isBlank()) track.copy(id = UUID.randomUUID().toString()) else track
        _activities.value += newTrack
    }

    override suspend fun updateTrack(track: Track) {
        _activities.value = _activities.value.map {
            if (it.id == track.id) track else it
        }
    }

    override suspend fun deleteTrack(id: String) {
        _activities.value = _activities.value.filter { it.id != id }
    }
}
