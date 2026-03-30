package eric.bitria.minimalfit.data.repository.track

import eric.bitria.minimalfit.data.database.dao.TrackDao
import eric.bitria.minimalfit.data.entity.track.Track
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

/**
 * Room implementation of the outdoor activity repository.
 */
class DefaultTrackRepository(private val trackDao: TrackDao) : TrackRepository {

    override fun getTracks(query: String, limit: Int): Flow<List<Track>> =
        if (query.isBlank()) trackDao.getTracks(limit)
        else trackDao.getTracks(query, limit)

    override fun getTracks(start: Instant, end: Instant): Flow<List<Track>> =
        trackDao.getTracks(start, end)

    override fun getTrack(id: String): Flow<Track?> =
        trackDao.getTrack(id)

    override suspend fun addTrack(track: Track) =
        trackDao.insertTrack(track)

    override suspend fun updateTrack(track: Track) =
        trackDao.updateTrack(track)

    override suspend fun deleteTrack(id: String) =
        trackDao.deleteTrack(id)
}
