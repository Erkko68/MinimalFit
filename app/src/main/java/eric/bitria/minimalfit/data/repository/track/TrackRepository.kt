package eric.bitria.minimalfit.data.repository.track

import eric.bitria.minimalfit.data.model.track.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Repository for accessing outdoor activities.
 */
interface TrackRepository {

    /** Returns tracks matching the query or recent ones if query is empty. */
    fun getTracks(query: String = "", limit: Int = 20): Flow<List<Track>>

    /** Returns tracks within a specific date range. */
    fun getTracks(start: LocalDate, end: LocalDate): Flow<List<Track>>

    /** Returns a specific track by ID. */
    fun getTrack(id: String): Flow<Track?>

    /** Adds a new track. */
    suspend fun addTrack(track: Track)

    /** Updates an existing track. */
    suspend fun updateTrack(track: Track)

    /** Deletes a track by ID. */
    suspend fun deleteTrack(id: String)
}
