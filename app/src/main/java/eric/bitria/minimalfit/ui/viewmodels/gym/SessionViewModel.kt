package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.gym.GymSessionManager
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val gymSessionManager: GymSessionManager
) : ViewModel() {

    data class SessionUiState(
        val sets: List<Set> = emptyList(),
        val elapsed: Duration = Duration.ZERO,
        val restRemaining: Duration = Duration.ZERO,
        val isRestRunning: Boolean = false,
        val isPaused: Boolean = false,
        val isActive: Boolean = false,
        val catalogExercises: List<Exercise> = emptyList()
    )

    private val _searchQuery = MutableStateFlow("")

    /** Expose search query so the UI can update it. */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val exercisesFlow = _searchQuery.flatMapLatest { query ->
        exerciseRepository.getExercises(query = query, limit = -1)
    }

    val uiState: StateFlow<SessionUiState> = combine(
        gymSessionManager.activeSets,
        gymSessionManager.elapsed,
        gymSessionManager.restRemaining,
        gymSessionManager.isRestRunning,
        gymSessionManager.isPaused,
        gymSessionManager.activeSession,
        exercisesFlow
    ) { args: Array<Any?> ->
        SessionUiState(
            sets = args[0] as List<Set>,
            elapsed = args[1] as Duration,
            restRemaining = args[2] as Duration,
            isRestRunning = args[3] as Boolean,
            isPaused = args[4] as Boolean,
            isActive = args[5] != null,
            catalogExercises = args[6] as List<Exercise>
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SessionUiState()
    )

    init {
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


    fun addSet(exerciseId: String) {
        gymSessionManager.addSet(exerciseId)
    }

    fun updateSet(set: Set) {
        gymSessionManager.updateSet(set)
    }

    fun deleteSet(setId: String) {
        gymSessionManager.deleteSet(setId)
    }

    fun finishSession() {
        viewModelScope.launch {
            gymSessionManager.finish()
        }
    }

    fun startRest(seconds: Int = 60) {
        gymSessionManager.startRest(seconds)
    }

    fun stopRest() {
        gymSessionManager.stopRest()
    }

    fun createNewExerciseAndAddSet(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val exercise = Exercise(name = name)
            exerciseRepository.addExercise(exercise)
            gymSessionManager.addSet(exercise.id)
        }
    }
}
