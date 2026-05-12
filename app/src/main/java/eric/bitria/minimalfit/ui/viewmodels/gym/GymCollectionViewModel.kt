package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.data.entity.gym.Routine
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.RoutineExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.RoutineRepository
import eric.bitria.minimalfit.data.repository.gym.RoutineSetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GymCollectionViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val routineRepository: RoutineRepository,
    private val routineExerciseRepository: RoutineExerciseRepository,
    private val routineSetRepository: RoutineSetRepository
) : ViewModel() {

    val userExercises: StateFlow<List<Exercise>> = exerciseRepository
        .getExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routinesWithDetails: StateFlow<List<RoutineWithDetails>> = routineRepository
        .getAll()
        .flatMapLatest { routines ->
            if (routines.isEmpty()) flowOf(emptyList())
            else combine(
                routines.map { routine ->
                    combine(
                        routineExerciseRepository.getForRoutine(routine.id),
                        routineSetRepository.getForRoutine(routine.id)
                    ) { exercises, sets ->
                        RoutineWithDetails(routine, exercises.size, sets.size)
                    }
                }
            ) { it.toList() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteRoutine(routineId: String) {
        viewModelScope.launch { routineRepository.delete(routineId) }
    }

    fun addExercise(name: String, muscleGroup: String?, isBodyweight: Boolean, restSeconds: Int) {
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
}
