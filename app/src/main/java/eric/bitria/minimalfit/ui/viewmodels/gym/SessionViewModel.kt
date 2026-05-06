package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.gym.GymSessionManager
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.time.Duration

class SessionViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val gymSessionManager: GymSessionManager
) : ViewModel() {

    val sessionSets: StateFlow<List<Set>> = gymSessionManager.activeSets

    val timerText: StateFlow<String> = gymSessionManager.elapsed
        .map { formatDuration(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "00:00"
        )

    val catalogExercises: StateFlow<List<Exercise>> = exerciseRepository.getExercises()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Start tracking (manager handles existing sessions)
        gymSessionManager.start()
    }

    private fun formatDuration(duration: Duration): String {
        val totalSeconds = duration.inWholeSeconds
        val mins = totalSeconds / 60
        val secs = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", mins, secs)
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
}
