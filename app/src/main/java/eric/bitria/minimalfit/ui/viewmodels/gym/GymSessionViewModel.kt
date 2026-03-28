package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.gym.GymExerciseEntity
import eric.bitria.minimalfit.data.entity.gym.GymSessionWithSets
import eric.bitria.minimalfit.data.entity.gym.GymSetEntity
import eric.bitria.minimalfit.data.repository.gym.GymRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.Duration
import eric.bitria.minimalfit.data.entity.gym.GymSessionStatus

data class GymExerciseUi(
    val exercise: GymExerciseEntity,
    val sets: List<GymSetEntity>
)

data class GymSessionUiState(
    val isLoading: Boolean = true,
    val session: GymSessionWithSets? = null,
    val exercises: List<GymExerciseUi> = emptyList()
)

class GymSessionViewModel(
    private val sessionId: String?,
    private val repository: GymRepository
) : ViewModel() {

    private val refreshTrigger = MutableStateFlow(0)
    private val _timerText = MutableStateFlow("00:00")
    val timerText: StateFlow<String> = _timerText.asStateFlow()

    val catalogExercises: StateFlow<List<GymExerciseEntity>> = repository.getExercises()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<GymSessionUiState> = combine(
        if (sessionId != null) repository.getSession(sessionId) else repository.getActiveSession(),
        repository.getExercises(),
        refreshTrigger
    ) { activeSession, exercises, _ ->
        val session = activeSession
        if (session == null) {
            GymSessionUiState(isLoading = false, session = null)
        } else {
            val grouped = exercises.map { ex ->
                GymExerciseUi(
                    exercise = ex,
                    sets = session.sets.filter { it.exerciseId == ex.id }
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
                val currentActive = repository.getActiveSession().first()
                if (currentActive == null) {
                    repository.startSession()
                    refreshTrigger.value++
                }
            }
            
            while (true) {
                val sessionOpt = uiState.value.session
                if (sessionOpt != null) {
                    val sessionEntity = sessionOpt.session
                    if (sessionEntity.status == GymSessionStatus.ACTIVE) {
                        val start = LocalDateTime.of(
                            sessionEntity.date.year,
                            sessionEntity.date.monthNumber,
                            sessionEntity.date.dayOfMonth,
                            sessionEntity.startTime.hour,
                            sessionEntity.startTime.minute,
                            sessionEntity.startTime.second
                        )
                        val now = LocalDateTime.now()
                        val duration = Duration.between(start, now)
                        val totalMinutes = duration.toMinutes()
                        val secs = duration.toSecondsPart()
                        _timerText.value = String.format("%02d:%02d", totalMinutes, secs)
                    } else if (sessionEntity.status == GymSessionStatus.COMPLETED && sessionEntity.endTime != null) {
                        val start = LocalDateTime.of(
                            sessionEntity.date.year,
                            sessionEntity.date.monthNumber,
                            sessionEntity.date.dayOfMonth,
                            sessionEntity.startTime.hour,
                            sessionEntity.startTime.minute,
                            sessionEntity.startTime.second
                        )
                        val end = LocalDateTime.of(
                            sessionEntity.date.year,
                            sessionEntity.date.monthNumber,
                            sessionEntity.date.dayOfMonth,
                            sessionEntity.endTime.hour,
                            sessionEntity.endTime.minute,
                            sessionEntity.endTime.second
                        )
                        val duration = Duration.between(start, end)
                        val totalMinutes = duration.toMinutes()
                        val secs = duration.toSecondsPart()
                        _timerText.value = String.format("%02d:%02d", totalMinutes, secs)
                    }
                }
                delay(1000)
            }
        }
    }

    fun startNewSession() {
        viewModelScope.launch {
            repository.startSession()
            refreshTrigger.value++
        }
    }

    fun createNewExerciseAndAddSet(name: String) {
        viewModelScope.launch {
            val exercise = repository.addExercise(name)
            addSet(exercise.id)
        }
    }

    fun addSet(exerciseId: String) {
        viewModelScope.launch {
            val session = uiState.value.session ?: return@launch
            repository.addSet(
                sessionId = session.session.id,
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
            repository.copyPreviousSet(session.session.id, exerciseId)
            refreshTrigger.value++
        }
    }

    fun updateSet(set: GymSetEntity) {
        viewModelScope.launch {
            repository.updateSet(set)
            refreshTrigger.value++
        }
    }

    fun deleteSet(setId: String) {
        viewModelScope.launch {
            repository.deleteSet(setId)
            refreshTrigger.value++
        }
    }

    fun finishSession() {
        viewModelScope.launch {
            repository.finishSession()
            refreshTrigger.value++
        }
    }
}
