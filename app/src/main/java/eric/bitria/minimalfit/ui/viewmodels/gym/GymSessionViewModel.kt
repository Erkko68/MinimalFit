package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.SessionStatus
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.gym.GymSessionManager
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.SessionRepository
import eric.bitria.minimalfit.data.repository.gym.SetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class GymExerciseUi(
    val exercise: Exercise,
    val sets: List<Set>
)

data class GymSessionUiState(
    val isLoading: Boolean = true,
    val session: Session? = null,
    val exercises: List<GymExerciseUi> = emptyList(),
    val restTimerText: String = "00:00",
    val isRestRunning: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class GymSessionViewModel(
    private val sessionId: String?,
    private val sessionRepository: SessionRepository,
    private val exerciseRepository: ExerciseRepository,
    private val setRepository: SetRepository,
    private val gymSessionManager: GymSessionManager
) : ViewModel() {

    private val _timerText = MutableStateFlow("00:00")
    val timerText: StateFlow<String> = _timerText.asStateFlow()

    private val currentSessionFlow: Flow<Session?> =
        if (sessionId != null) sessionRepository.getSession(sessionId)
        else gymSessionManager.activeSession

    private val currentSetsFlow: Flow<List<Set>> =
        currentSessionFlow.flatMapLatest { session ->
            if (session == null) flowOf(emptyList())
            else setRepository.getSetsForSession(session.id)
        }

    val catalogExercises: StateFlow<List<Exercise>> = exerciseRepository.getExercises()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<GymSessionUiState> = combine(
        currentSessionFlow,
        exerciseRepository.getExercises(),
        currentSetsFlow,
        gymSessionManager.restRemaining,
        gymSessionManager.isRestRunning
    ) { session, exercises, sessionSets, restRemaining, isRestRunning ->
        if (session == null) {
            GymSessionUiState(isLoading = false, session = null)
        } else {
            val grouped = exercises.map { ex ->
                GymExerciseUi(
                    exercise = ex,
                    sets = sessionSets.filter { it.exerciseId == ex.id }
                )
            }.filter { it.sets.isNotEmpty() }
            GymSessionUiState(
                isLoading = false,
                session = session,
                exercises = grouped,
                restTimerText = formatDuration(restRemaining),
                isRestRunning = isRestRunning
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GymSessionUiState()
    )

    init {
        if (sessionId == null) {
            gymSessionManager.start()
        }

        viewModelScope.launch {
            combine(currentSessionFlow, gymSessionManager.elapsed) { session, activeElapsed ->
                val duration = when {
                    session == null -> Duration.ZERO
                    session.status == SessionStatus.ACTIVE -> {
                        if (sessionId == null) activeElapsed else calculateSessionElapsed(session)
                    }
                    session.status == SessionStatus.COMPLETED -> {
                        calculateSessionElapsed(session)
                    }
                    else -> calculateSessionElapsed(session)
                }
                duration
            }.map { duration ->
                val totalSeconds = duration.inWholeSeconds
                val mins = totalSeconds / 60
                val secs = totalSeconds % 60
                String.format(Locale.US, "%02d:%02d", mins, secs)
            }.collect { text ->
                _timerText.value = text
            }
        }
    }

    fun startNewSession() {
        viewModelScope.launch {
            gymSessionManager.start()
        }
    }

    fun createNewExerciseAndAddSet(name: String) {
        viewModelScope.launch {
            val exercise = exerciseRepository.addExercise(name)
            addSet(exercise.id)
        }
    }

    fun addSet(exerciseId: String) {
        viewModelScope.launch {
            val session = uiState.value.session ?: return@launch
            setRepository.addSet(
                sessionId = session.id,
                exerciseId = exerciseId,
                weight = 0f,
                reps = 0,
                rpe = null,
                isWarmup = false,
                notes = ""
            )
        }
    }

    fun copyPreviousSet(exerciseId: String) {
        viewModelScope.launch {
            val session = uiState.value.session ?: return@launch
            setRepository.copyPreviousSet(session.id, exerciseId)
        }
    }

    fun updateSet(set: Set) {
        viewModelScope.launch {
            val completedNow = setRepository.updateSet(set)
            if (completedNow) {
                gymSessionManager.startRestForExercise(set.exerciseId)
            }
        }
    }

    fun pauseSession() {
        gymSessionManager.pause()
    }

    fun resumeSession() {
        gymSessionManager.resume()
    }

    fun toggleSetCompleted(set: Set) {
        updateSet(set.copy(isCompleted = !set.isCompleted))
    }

    fun updateExerciseRest(exerciseId: String, restSeconds: Int) {
        viewModelScope.launch {
            gymSessionManager.updateExerciseRest(exerciseId, restSeconds)
        }
    }

    fun addRestSeconds(seconds: Int = 30) {
        gymSessionManager.addRestSeconds(seconds)
    }

    fun stopRest() {
        gymSessionManager.stopRest()
    }

    fun deleteSet(setId: String) {
        viewModelScope.launch {
            setRepository.deleteSet(setId)
        }
    }

    fun finishSession() {
        viewModelScope.launch {
            gymSessionManager.finish()
        }
    }

    fun finishLatestSetAndStartRest() {
        gymSessionManager.finishLatestSetAndStartRest()
    }

    private fun formatDuration(duration: Duration): String {
        val totalSeconds = duration.inWholeSeconds
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    private fun calculateSessionElapsed(session: Session): Duration {
        val endReference = when {
            session.status == SessionStatus.PAUSED && session.pausedAt != null -> session.pausedAt
            session.status == SessionStatus.COMPLETED && session.endTime != null -> session.endTime
            else -> Clock.System.now()
        }
        val raw = endReference - session.startTime
        return (raw - session.pausedDurationSeconds.seconds).coerceAtLeast(Duration.ZERO)
    }
}
