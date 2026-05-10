package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.SessionExercise
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.gym.GymSessionManager
import eric.bitria.minimalfit.data.gym.RoutineExercisePlan
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.RoutineRepository
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
    private val gymSessionManager: GymSessionManager
) : ViewModel() {

    data class SessionExerciseGroup(
        val sessionExerciseId: String,
        val exerciseId: String,
        val exerciseName: String,
        val sets: List<Set>,
        val createdAt: Instant
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
            SessionExerciseGroup(
                sessionExerciseId = se.id,
                exerciseId = se.exerciseId,
                exerciseName = exercisesById[se.exerciseId]?.name ?: "Exercise",
                sets = sets.filter { it.sessionExerciseId == se.id }.sortedBy { it.createdAt },
                createdAt = se.createdAt
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
            val exercises = routineRepository.getRoutineExercises(routineId)
                .map { ref ->
                    RoutineExercisePlan(
                        exerciseId = ref.exerciseId,
                        targetSets = ref.targetSets,
                        targetReps = ref.targetReps,
                        targetWeight = ref.targetWeight
                    )
                }
            val routineName = routineRepository.getRoutines()
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

    fun addSet(sessionExerciseId: String) {
        gymSessionManager.addSet(sessionExerciseId)
    }

    fun updateSet(set: Set) {
        gymSessionManager.updateSet(set)
    }

    fun deleteSet(setId: String) {
        gymSessionManager.deleteSet(setId)
    }

    fun deleteExercise(sessionExerciseId: String) {
        gymSessionManager.deleteExercise(sessionExerciseId)
    }

    fun finishSession(saveAsRoutine: Boolean = false) {
        viewModelScope.launch {
            if (saveAsRoutine) {
                saveCurrentSessionAsRoutine()
            }
            gymSessionManager.finish()
        }
    }

    private suspend fun saveCurrentSessionAsRoutine() {
        val state = uiState.value
        val exerciseIds = state.exerciseGroups
            .map { it.exerciseId }
            .distinct()
        if (exerciseIds.isEmpty()) return

        val routineName = state.sessionTitle.ifBlank { "Workout Routine" }
        routineRepository.createRoutine(routineName, exerciseIds)
    }

    fun startRest(seconds: Int = 60) {
        gymSessionManager.startRest(seconds)
    }

    fun stopRest() {
        gymSessionManager.stopRest()
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
            gymSessionManager.addExercise(exerciseId)
        }
    }

    fun updateSessionTitle(title: String) {
        gymSessionManager.updateSessionTitle(title)
    }
}
