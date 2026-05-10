package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.data.entity.gym.RoutineSummary
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.RoutineRepository
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

class GymViewModel(
    private val sessionRepository: SessionRepository,
    private val exerciseRepository: ExerciseRepository,
    private val setRepository: SetRepository,
    private val routineRepository: RoutineRepository
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

    val routines: StateFlow<List<RoutineSummary>> = routineRepository
        .getRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteSession(sessionId: String) {
        viewModelScope.launch { sessionRepository.deleteSession(sessionId) }
    }

    fun addExercise(
        name: String,
        muscleGroup: String?,
        isBodyweight: Boolean,
        restSeconds: Int
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return
        viewModelScope.launch {
            exerciseRepository.addExercise(
                Exercise(
                    name = trimmedName,
                    muscleGroup = muscleGroup?.trim()?.takeIf { it.isNotBlank() },
                    isBodyweight = isBodyweight,
                    restSeconds = restSeconds.coerceAtLeast(0)
                )
            )
        }
    }

    fun deleteExercise(exerciseId: String) {
        viewModelScope.launch { exerciseRepository.deleteExercise(exerciseId) }
    }

    fun createRoutine(name: String, exerciseIds: List<String>) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank() || exerciseIds.isEmpty()) return
        viewModelScope.launch {
            routineRepository.createRoutine(trimmedName, exerciseIds)
        }
    }

    fun deleteRoutine(routineId: String) {
        viewModelScope.launch { routineRepository.deleteRoutine(routineId) }
    }
}
