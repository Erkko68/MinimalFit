package eric.bitria.minimalfit.data.repository.track

import eric.bitria.minimalfit.data.database.TrackDatabase
import eric.bitria.minimalfit.data.entity.track.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * In-memory implementation of the outdoor activity repository.
 */
class DefaultTrackRepository(private val trackDatabase: TrackDatabase) : TrackRepository {

    override fun getTracks(query: String, limit: Int): Flow<List<Track>> =
        trackDatabase.getTracks(query, limit)

    override fun getTracks(start: LocalDate, end: LocalDate): Flow<List<Track>> =
        trackDatabase.getTracks(start, end)

    override fun getTrack(id: String): Flow<Track?> =
        trackDatabase.getTrack(id)

    override suspend fun addTrack(track: Track) =
        trackDatabase.addTrack(track)

    override suspend fun updateTrack(track: Track) =
        trackDatabase.updateTrack(track)

    override suspend fun deleteTrack(id: String) =
        trackDatabase.deleteTrack(id)
}
