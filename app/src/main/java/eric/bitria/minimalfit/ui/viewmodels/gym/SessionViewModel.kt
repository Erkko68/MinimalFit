package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.SessionExercise
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.gym.GymSessionManager
import eric.bitria.minimalfit.data.gym.RoutineExercisePlan
import eric.bitria.minimalfit.data.entity.gym.Routine
import eric.bitria.minimalfit.data.entity.gym.RoutineExercise
import eric.bitria.minimalfit.data.entity.gym.RoutineSet
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
import eric.bitria.minimalfit.data.gym.PlannedSetPlan
import eric.bitria.minimalfit.data.repository.gym.RoutineExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.RoutineRepository
import eric.bitria.minimalfit.data.repository.gym.RoutineSetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val routineRepository: RoutineRepository,
    private val routineExerciseRepository: RoutineExerciseRepository,
    private val routineSetRepository: RoutineSetRepository,
    private val gymSessionManager: GymSessionManager,
) : ViewModel() {

    data class SessionExerciseGroup(
        val sessionExerciseId: String,
        val exerciseId: String,
        val exerciseName: String,
        val sets: List<Set>,
        val createdAt: Instant,
        val restSeconds: Int = 120
    )

    data class SessionUiState(
        val exerciseGroups: List<SessionExerciseGroup> = emptyList(),
        val elapsed: Duration = Duration.ZERO,
        val restRemaining: Duration = Duration.ZERO,
        val isRestRunning: Boolean = false,
        val isPaused: Boolean = false,
        val isActive: Boolean = false,
        val sessionTitle: String = "",
        val sessionStartTime: Instant? = null,
        val catalogExercises: List<Exercise> = emptyList()
    )

    private val _searchQuery = MutableStateFlow("")

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val exercisesFlow = _searchQuery.flatMapLatest { query ->
        exerciseRepository.getExercises(query = query, limit = -1)
    }

    val uiState: StateFlow<SessionUiState> = combine(
        gymSessionManager.activeSessionExercises,
        gymSessionManager.activeSets,
        gymSessionManager.elapsed,
        gymSessionManager.restRemaining,
        gymSessionManager.isRestRunning,
        gymSessionManager.isPaused,
        gymSessionManager.activeSession,
        exercisesFlow
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val sessionExercises = args[0] as List<SessionExercise>
        @Suppress("UNCHECKED_CAST")
        val sets = args[1] as List<Set>
        val session = args[6] as? Session
        @Suppress("UNCHECKED_CAST")
        val catalogExercises = args[7] as List<Exercise>
        val exercisesById = catalogExercises.associateBy { it.id }

        val groups = sessionExercises.map { se ->
            val exercise = exercisesById[se.exerciseId]
            SessionExerciseGroup(
                sessionExerciseId = se.id,
                exerciseId = se.exerciseId,
                exerciseName = exercise?.name ?: "Exercise",
                sets = sets.filter { it.sessionExerciseId == se.id }.sortedBy { it.createdAt },
                createdAt = se.createdAt,
                restSeconds = exercise?.restSeconds ?: 120
            )
        }

        SessionUiState(
            exerciseGroups = groups,
            elapsed = args[2] as Duration,
            restRemaining = args[3] as Duration,
            isRestRunning = args[4] as Boolean,
            isPaused = args[5] as Boolean,
            isActive = session != null,
            sessionTitle = session?.title ?: "",
            sessionStartTime = session?.startTime,
            catalogExercises = catalogExercises
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SessionUiState()
    )

    fun initialize(
        sessionId: String?,
        routineId: String?,
        replaceActiveWorkout: Boolean
    ) {
        when {
            sessionId != null -> gymSessionManager.loadSession(sessionId)
            routineId != null -> startRoutineSession(routineId, replaceActiveWorkout)
        }
    }

    private fun startRoutineSession(routineId: String, replaceActiveWorkout: Boolean) {
        viewModelScope.launch {
            val routineSets = routineSetRepository.getForRoutine(routineId).first()
            val exercises = routineExerciseRepository.getForRoutine(routineId)
                .first()
                .map { ref ->
                    val plannedSets = routineSets.filter { it.routineExerciseId == ref.id }
                    if (plannedSets.isEmpty()) {
                        RoutineExercisePlan(exerciseId = ref.exerciseId, targetSets = 1)
                    } else {
                        RoutineExercisePlan(
                            exerciseId = ref.exerciseId,
                            plannedSets = plannedSets.map { set ->
                                PlannedSetPlan(
                                    weight = set.weight,
                                    reps = set.reps,
                                    type = set.type,
                                    durationSeconds = set.durationSeconds,
                                    preparationSeconds = set.preparationSeconds
                                )
                            }
                        )
                    }
                }
            val routineName = routineRepository.getAll()
                .first()
                .firstOrNull { it.id == routineId }
                ?.name
                ?: "Workout"
            if (replaceActiveWorkout) {
                gymSessionManager.replaceWithRoutine(exercises, routineName)
            } else if (exercises.isEmpty()) {
                gymSessionManager.start()
            } else {
                gymSessionManager.startFromRoutine(exercises, routineName)
            }
        }
    }

    fun startSession() {
        gymSessionManager.start()
    }

    fun pauseSession() {
        gymSessionManager.pause()
    }

    fun resumeSession() {
        gymSessionManager.resume()
    }

    fun addExercise(exerciseId: String) {
        gymSessionManager.addExercise(exerciseId)
    }

    fun addSet(sessionExerciseId: String, weight: Float, reps: Int, isCompleted: Boolean = false) {
        gymSessionManager.addSet(sessionExerciseId, weight, reps, isCompleted)
        if (isCompleted) triggerRestForExercise(sessionExerciseId)
    }

    fun addTimedSet(
        sessionExerciseId: String,
        weight: Float,
        durationSeconds: Int,
        preparationSeconds: Int,
        isCompleted: Boolean = false
    ) {
        gymSessionManager.addTimedSet(
            sessionExerciseId = sessionExerciseId,
            weight = weight,
            durationSeconds = durationSeconds,
            preparationSeconds = preparationSeconds,
            isCompleted = isCompleted
        )
        if (isCompleted) triggerRestForExercise(sessionExerciseId)
    }

    fun updateSet(set: Set) {
        val wasCompleted = uiState.value.exerciseGroups
            .flatMap { it.sets }
            .firstOrNull { it.id == set.id }
            ?.isCompleted == true
        gymSessionManager.updateSet(set)
        if (set.isCompleted && !wasCompleted) triggerRestForExercise(set.sessionExerciseId)
    }

    private fun triggerRestForExercise(sessionExerciseId: String) {
        val restSeconds = uiState.value.exerciseGroups
            .firstOrNull { it.sessionExerciseId == sessionExerciseId }
            ?.restSeconds ?: 120
        gymSessionManager.startRest(restSeconds)
    }

    fun deleteSet(setId: String) {
        gymSessionManager.deleteSet(setId)
    }

    fun deleteExercise(sessionExerciseId: String) {
        gymSessionManager.deleteExercise(sessionExerciseId)
    }

    fun finishSession() {
        gymSessionManager.finish()
    }

    fun saveSessionAsRoutine() {
        viewModelScope.launch { saveCurrentSessionAsRoutine() }
    }

    private suspend fun saveCurrentSessionAsRoutine() {
        val state = uiState.value
        val groups = state.exerciseGroups
        if (groups.isEmpty()) return

        val routineName = state.sessionTitle.ifBlank { "Workout Routine" }
        val routine = Routine(name = routineName)
        routineRepository.add(routine)
        groups.forEach { group ->
            val routineExercise = RoutineExercise(routineId = routine.id, exerciseId = group.exerciseId)
            routineExerciseRepository.add(routineExercise)
            group.sets.forEach { set ->
                routineSetRepository.add(
                    RoutineSet(
                        routineExerciseId = routineExercise.id,
                        weight = set.weight,
                        reps = set.reps,
                        type = set.type,
                        durationSeconds = set.durationSeconds,
                        preparationSeconds = set.preparationSeconds
                    )
                )
            }
        }
    }

    fun startRest(seconds: Int = 60) {
        gymSessionManager.startRest(seconds)
    }

    fun stopRest() {
        gymSessionManager.stopRest()
    }

    fun createExerciseAndAdd(
        name: String,
        muscleGroup: String?,
        isBodyweight: Boolean,
        restSeconds: Int
    ) {
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
                val exercise = Exercise(
                    name = trimmed,
                    muscleGroup = muscleGroup,
                    isBodyweight = isBodyweight,
                    restSeconds = restSeconds
                )
                exerciseRepository.addExercise(exercise)
                exercise.id
            }
            gymSessionManager.addExercise(exerciseId)
        }
    }

    fun updateSessionTitle(title: String) {
        gymSessionManager.updateSessionTitle(title)
    }
}
