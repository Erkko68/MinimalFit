package eric.bitria.minimalfit.data.repository

import eric.bitria.minimalfit.data.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
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
        // Add some sample data
        _activitiesFlow.value = listOf(
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
        _activitiesFlow.value = _activitiesFlow.value + newActivity
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
