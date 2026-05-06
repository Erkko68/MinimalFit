package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.SessionRepository
import eric.bitria.minimalfit.data.repository.gym.SetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI-friendly representation of a workout session with its associated sets.
 */
data class SessionWithSets(
    val session: Session,
    val sets: List<Set>
)

/**
 * Represents a routine (collection of exercises) - TODO: Implement full routine system.
 */
data class Routine(
    val id: String,
    val name: String,
    val exerciseIds: List<String>
)

class GymViewModel(
    private val sessionRepository: SessionRepository,
    private val exerciseRepository: ExerciseRepository,
    private val setRepository: SetRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val pastSessions: StateFlow<List<SessionWithSets>> = sessionRepository
        .getSessions(limit = 20)
        .flatMapLatest { sessions ->
            if (sessions.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    sessions.map { session ->
                        setRepository.getSetsForSession(session.id).map { sets ->
                            SessionWithSets(session, sets)
                        }
                    }
                ) { it.toList() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userExercises: StateFlow<List<Exercise>> = exerciseRepository
        .getExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // TODO: Connect to a RoutineRepository once implemented
    val routines: StateFlow<List<Routine>> = flowOf(
        listOf(
            Routine("1", "Push Day", emptyList()),
            Routine("2", "Pull Day", emptyList()),
            Routine("3", "Leg Day", emptyList())
        )
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteSession(sessionId: String) {
        viewModelScope.launch { sessionRepository.deleteSession(sessionId) }
    }

    fun addExercise(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { exerciseRepository.addExercise(Exercise(name = name)) }
    }

    fun deleteExercise(exerciseId: String) {
        viewModelScope.launch { exerciseRepository.deleteExercise(exerciseId) }
    }
}