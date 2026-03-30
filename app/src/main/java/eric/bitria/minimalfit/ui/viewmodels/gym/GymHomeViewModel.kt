package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.SetRepository
import eric.bitria.minimalfit.data.repository.gym.SessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.util.shortMonthDay
import java.util.Locale
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
    private val sessionRepository: SessionRepository,
    private val exerciseRepository: ExerciseRepository,
    private val setRepository: SetRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentSessions: StateFlow<List<GymSessionSummaryUi>> = sessionRepository
        .getRecentSessions(limit = 20)
        .flatMapLatest { sessions ->
            if (sessions.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    sessions.map { session ->
                        setRepository.getSetsForSession(session.id).map { sets ->
                            session to sets
                        }
                    }
                ) { pairs ->
                    pairs.map { (session, sets) -> toSummary(session, sets) }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val exercises: StateFlow<List<Exercise>> = exerciseRepository
        .getExercises()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
        }
    }

    fun addExercise(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            exerciseRepository.addExercise(name)
        }
    }

    fun deleteExercise(exerciseId: String) {
        viewModelScope.launch {
            exerciseRepository.deleteExercise(exerciseId)
        }
    }

    private fun toSummary(session: Session, sets: List<Set>): GymSessionSummaryUi {
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
            durationText = String.format(Locale.US, "%02d:%02d", totalMinutes, secs)
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
