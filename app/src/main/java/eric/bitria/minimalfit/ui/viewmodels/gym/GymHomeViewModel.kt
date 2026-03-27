package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.gym.GymSessionEntity
import eric.bitria.minimalfit.data.entity.gym.GymSessionWithSets
import eric.bitria.minimalfit.data.repository.gym.GymRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.Duration

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

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    private fun GymSessionWithSets.toSummary(): GymSessionSummaryUi {
        val title = "Workout"
        val subtitle = "${session.date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}, ${session.date.day} ${session.date.month.name.lowercase().replaceFirstChar { it.uppercase() }}"
        
        val exercisesCount = sets.map { it.exerciseId }.distinct().size
        val completedSets = sets.filter { it.isCompleted }
        val setsCount = completedSets.size
        val volume = completedSets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
        
        var durationText = "--"
        if (session.endTime != null) {
            val start = LocalDateTime.of(session.date.year, session.date.monthNumber, session.date.dayOfMonth, session.startTime.hour, session.startTime.minute, session.startTime.second)
            val end = LocalDateTime.of(session.date.year, session.date.monthNumber, session.date.dayOfMonth, session.endTime.hour, session.endTime.minute, session.endTime.second)
            val duration = Duration.between(start, end)
            val hours = duration.toHours()
            val mins = duration.toMinutesPart()
            val secs = duration.toSecondsPart()
            durationText = if (hours > 0) String.format("%d:%02d:%02d", hours, mins, secs) else String.format("%02d:%02d", mins, secs)
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
