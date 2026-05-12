package eric.bitria.minimalfit.data.repository.track

import eric.bitria.minimalfit.data.database.dao.TrackDao
import eric.bitria.minimalfit.data.entity.track.Track
import eric.bitria.minimalfit.data.remote.sync.SyncScheduler
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

/**
 * Room implementation of the outdoor activity repository.
 */
class DefaultTrackRepository(
    private val trackDao: TrackDao,
    private val syncScheduler: SyncScheduler
) : TrackRepository {

    override fun getTracks(
        query: String,
        start: Instant?,
        end: Instant?,
        limit: Int
    ): Flow<List<Track>> = trackDao.getTracks(query, start, end, limit)

    override fun getTrack(id: String): Flow<Track?> =
        trackDao.getTrack(id)

    override suspend fun addTrack(track: Track) {
        trackDao.insertTrack(track)
        syncScheduler.enqueue("track", track.id, "upsert")
    }

    override suspend fun updateTrack(track: Track) {
        trackDao.updateTrack(track)
        syncScheduler.enqueue("track", track.id, "upsert")
    }

    override suspend fun deleteTrack(id: String) {
        trackDao.deleteTrack(id)
        syncScheduler.enqueue("track", id, "delete")
    }
}
