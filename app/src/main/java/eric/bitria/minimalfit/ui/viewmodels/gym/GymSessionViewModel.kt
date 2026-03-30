package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.SessionStatus
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.SessionRepository
import eric.bitria.minimalfit.data.repository.gym.SetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.time.Clock

data class GymExerciseUi(
    val exercise: Exercise,
    val sets: List<Set>
)

data class GymSessionUiState(
    val isLoading: Boolean = true,
    val session: Session? = null,
    val exercises: List<GymExerciseUi> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class GymSessionViewModel(
    private val sessionId: String?,
    private val sessionRepository: SessionRepository,
    private val exerciseRepository: ExerciseRepository,
    private val setRepository: SetRepository
) : ViewModel() {

    private val refreshTrigger = MutableStateFlow(0)
    private val _timerText = MutableStateFlow("00:00")
    val timerText: StateFlow<String> = _timerText.asStateFlow()

    private val currentSessionFlow: Flow<Session?> =
        if (sessionId != null) sessionRepository.getSession(sessionId)
        else sessionRepository.getActiveSession()

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
        refreshTrigger
    ) { session, exercises, sessionSets, _ ->
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
                exercises = grouped
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GymSessionUiState()
    )

    init {
        viewModelScope.launch {
            if (sessionId == null) {
                val currentActive = sessionRepository.getActiveSession().first()
                if (currentActive == null) {
                    sessionRepository.startSession()
                    refreshTrigger.value++
                }
            }
            
            while (true) {
                val sessionOpt = uiState.value.session
                if (sessionOpt != null) {
                    val duration = when (sessionOpt.status) {
                        SessionStatus.ACTIVE -> {
                            Clock.System.now() - sessionOpt.startTime
                        }
                        SessionStatus.COMPLETED -> {
                            sessionOpt.endTime?.let { it - sessionOpt.startTime }
                        }
                        else -> {
                            null
                        }
                    }

                    duration?.let { d ->
                        val totalSeconds = d.inWholeSeconds
                        val mins = totalSeconds / 60
                        val secs = totalSeconds % 60
                        _timerText.value = String.format(Locale.US, "%02d:%02d", mins, secs)
                    }
                }
                delay(1000)
            }
        }
    }

    fun startNewSession() {
        viewModelScope.launch {
            sessionRepository.startSession()
            refreshTrigger.value++
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
            refreshTrigger.value++
        }
    }

    fun copyPreviousSet(exerciseId: String) {
        viewModelScope.launch {
            val session = uiState.value.session ?: return@launch
            setRepository.copyPreviousSet(session.id, exerciseId)
            refreshTrigger.value++
        }
    }

    fun updateSet(set: Set) {
        viewModelScope.launch {
            setRepository.updateSet(set)
            refreshTrigger.value++
        }
    }

    fun deleteSet(setId: String) {
        viewModelScope.launch {
            setRepository.deleteSet(setId)
            refreshTrigger.value++
        }
    }

    fun finishSession() {
        viewModelScope.launch {
            sessionRepository.finishSession()
            refreshTrigger.value++
        }
    }
}
