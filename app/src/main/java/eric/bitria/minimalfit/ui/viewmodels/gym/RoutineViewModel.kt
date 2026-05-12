package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.data.entity.gym.Routine
import eric.bitria.minimalfit.data.entity.gym.RoutineExercise
import eric.bitria.minimalfit.data.entity.gym.RoutineSet
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.RoutineExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.RoutineRepository
import eric.bitria.minimalfit.data.repository.gym.RoutineSetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineViewModel(
    private val routineRepository: RoutineRepository,
    private val routineExerciseRepository: RoutineExerciseRepository,
    private val routineSetRepository: RoutineSetRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    data class RoutineExerciseGroup(
        val routineExerciseId: String,
        val exerciseName: String,
        val sets: List<RoutineSet>,
        val createdAt: Instant
    )

    data class RoutineUiState(
        val routine: Routine? = null,
        val exerciseGroups: List<RoutineExerciseGroup> = emptyList(),
        val catalogExercises: List<Exercise> = emptyList()
    )

    private val _routineId = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val routineFlow = _routineId.flatMapLatest { id ->
        if (id == null) flowOf(null) else routineRepository.getById(id)
    }

    private val exercisesFlow = _routineId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else routineExerciseRepository.getForRoutine(id)
    }

    private val setsFlow = _routineId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else routineSetRepository.getForRoutine(id)
    }

    private val catalogFlow = _searchQuery.flatMapLatest { query ->
        exerciseRepository.getExercises(query = query, limit = -1)
    }

    val uiState: StateFlow<RoutineUiState> = combine(
        routineFlow,
        exercisesFlow,
        setsFlow,
        catalogFlow
    ) { routine, routineExercises, sets, catalog ->
        val exercisesById = catalog.associateBy { it.id }
        val groups = routineExercises.map { re ->
            RoutineExerciseGroup(
                routineExerciseId = re.id,
                exerciseName = exercisesById[re.exerciseId]?.name ?: "Exercise",
                sets = sets.filter { it.routineExerciseId == re.id },
                createdAt = re.createdAt
            )
        }
        RoutineUiState(
            routine = routine,
            exerciseGroups = groups,
            catalogExercises = catalog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RoutineUiState()
    )

    fun initialize(routineId: String?) {
        if (routineId == null) {
            viewModelScope.launch {
                val routine = Routine()
                routineRepository.add(routine)
                _routineId.value = routine.id
            }
        } else {
            _routineId.value = routineId
        }
    }

    fun updateName(name: String) {
        val routine = uiState.value.routine ?: return
        viewModelScope.launch { routineRepository.update(routine.copy(name = name)) }
    }

    fun updateDescription(desc: String) {
        val routine = uiState.value.routine ?: return
        viewModelScope.launch { routineRepository.update(routine.copy(description = desc)) }
    }

    fun toggleDay(day: Int) {
        val routine = uiState.value.routine ?: return
        val current = routine.daysOfWeek
        val updated = if (current.contains(day)) current - day else current + day
        viewModelScope.launch { routineRepository.update(routine.withDaysOfWeek(updated)) }
    }

    fun addExercise(exerciseId: String) {
        val routineId = _routineId.value ?: return
        viewModelScope.launch {
            routineExerciseRepository.add(RoutineExercise(routineId = routineId, exerciseId = exerciseId))
        }
    }

    fun deleteExercise(routineExerciseId: String) {
        viewModelScope.launch {
            routineSetRepository.deleteForRoutineExercise(routineExerciseId)
            routineExerciseRepository.delete(routineExerciseId)
        }
    }

    fun addSet(routineExerciseId: String) {
        viewModelScope.launch {
            routineSetRepository.add(RoutineSet(routineExerciseId = routineExerciseId))
        }
    }

    fun updateSet(set: RoutineSet) {
        viewModelScope.launch { routineSetRepository.update(set) }
    }

    fun deleteSet(setId: String) {
        viewModelScope.launch { routineSetRepository.delete(setId) }
    }

    fun createNewExerciseAndAdd(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val existing = exerciseRepository
                .getExercises(query = trimmed, limit = 10)
                .first()
                .firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
            val exerciseId = if (existing != null) {
                existing.id
            } else {
                val exercise = Exercise(name = trimmed)
                exerciseRepository.addExercise(exercise)
                exercise.id
            }
            addExercise(exerciseId)
        }
    }
}
