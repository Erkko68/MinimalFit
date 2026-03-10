package eric.bitria.minimalfit.data.repository.track

import eric.bitria.minimalfit.data.model.Track
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository for accessing outdoor activities.
 */
interface TrackRepository {

    /** Returns all outdoor activities as a Flow that emits on changes. */
    fun getAllActivitiesFlow(): Flow<List<Track>>

    /** Returns all outdoor activities (snapshot). */
    fun getAllActivities(): List<Track>

    /** Returns activities for a specific date. */
    fun getActivitiesForDate(date: LocalDate): List<Track>

    /** Returns a specific activity by id as a Flow that emits on changes. */
    fun getTrackByIdFlow(id: String): Flow<Track?>

    /** Returns a specific activity by id (snapshot). */
    fun getTrackById(id: String): Track?

    /** Adds a new activity. */
    fun addActivity(activity: Track)

    /** Updates an existing activity. */
    fun updateActivity(activity: Track)

    /** Deletes an activity by id. */
    fun deleteActivity(id: String)
}
