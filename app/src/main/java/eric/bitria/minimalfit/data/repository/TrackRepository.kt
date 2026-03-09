package eric.bitria.minimalfit.data.repository

import eric.bitria.minimalfit.data.model.Track
import java.time.LocalDate

/**
 * Repository for accessing outdoor activities.
 */
interface TrackRepository {

    /** Returns all outdoor activities. */
    fun getAllActivities(): List<Track>

    /** Returns activities for a specific date. */
    fun getActivitiesForDate(date: LocalDate): List<Track>

    /** Adds a new activity. */
    fun addActivity(activity: Track)

    /** Deletes an activity by id. */
    fun deleteActivity(id: String)
}
