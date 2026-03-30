package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.gym.GymSessionWithSets
import eric.bitria.minimalfit.data.repository.gym.GymRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.util.shortMonthDay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class GymSessionSummaryUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val duration: String,
    val setsCount: Int,
    val exercisesCount: Int,
    val volume: Float
)

class GymHomeViewModel(
    private val repository: GymRepository
) : ViewModel() {

    val recentSessions: StateFlow<List<GymSessionSummaryUi>> = repository
        .getRecentSessionsWithSets(limit = 20)
        .map { sessions ->
            sessions.map { it.toSummary() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val exercises: StateFlow<List<Exercise>> = repository
        .getExercises()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    fun addExercise(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addExercise(name)
        }
    }

    fun deleteExercise(exerciseId: String) {
        viewModelScope.launch {
            repository.deleteExercise(exerciseId)
        }
    }

    private fun GymSessionWithSets.toSummary(): GymSessionSummaryUi {
        val title = "Workout"
        val startDateTime = session.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
        val subtitle = "${startDateTime.date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}, ${startDateTime.date.shortMonthDay()}"
        
        val exercisesCount = sets.map { it.exerciseId }.distinct().size
        val completedSets = sets.filter { it.isCompleted }
        val setsCount = completedSets.size
        val volume = completedSets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
        
        var durationText = "--"
        if (session.endTime != null) {
            val duration = session.endTime - session.startTime
            val totalMinutes = duration.inWholeMinutes
            val secs = duration.inWholeSeconds % 60
            durationText = String.format("%02d:%02d", totalMinutes, secs)
        }

        return GymSessionSummaryUi(
            id = session.id,
            title = title,
            subtitle = subtitle,
            duration = durationText,
            setsCount = setsCount,
            exercisesCount = exercisesCount,
            volume = volume
        )
    }
}
