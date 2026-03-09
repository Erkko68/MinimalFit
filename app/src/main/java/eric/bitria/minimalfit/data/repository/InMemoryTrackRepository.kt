package eric.bitria.minimalfit.data.repository

import eric.bitria.minimalfit.data.model.Track
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.minutes

/**
 * In-memory implementation of the outdoor activity repository.
 */
class InMemoryTrackRepository : TrackRepository {

    private val activities = mutableListOf<Track>()
    private val idCounter = AtomicInteger(1)

    init {
        // Add some sample data
        activities.addAll(
            listOf(
                Track(
                    id = idCounter.getAndIncrement().toString(),
                    date = LocalDate.now().minusDays(1),
                    time = LocalTime.of(18, 30),
                    name = "Evening Run",
                    distance = 5.2,
                    duration = 30.minutes,
                    pace = "5:46",
                    mapImageUrl = "https://picsum.photos/600/400"
                ),
                Track(
                    id = idCounter.getAndIncrement().toString(),
                    date = LocalDate.now().minusDays(2),
                    time = LocalTime.of(7, 0),
                    name = "Morning Walk",
                    distance = 3.1,
                    duration = 45.minutes,
                    pace = "14:31",
                    mapImageUrl = "https://picsum.photos/600/600"
                )
            )
        )
    }

    override fun getAllActivities(): List<Track> = activities.toList()

    override fun getActivitiesForDate(date: LocalDate): List<Track> =
        activities.filter { it.date == date }

    override fun getTrackById(id: String): Track? =
        activities.find { it.id == id }

    override fun addActivity(activity: Track) {
        val newActivity = activity.copy(id = idCounter.getAndIncrement().toString())
        activities.add(newActivity)
    }

    override fun updateActivity(activity: Track) {
        val index = activities.indexOfFirst { it.id == activity.id }
        if (index != -1) {
            activities[index] = activity
        }
    }

    override fun deleteActivity(id: String) {
        activities.removeIf { it.id == id }
    }
}
