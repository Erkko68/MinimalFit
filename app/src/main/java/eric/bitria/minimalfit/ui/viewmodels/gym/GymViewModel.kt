package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.gym.GymSessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class SessionWithSets(
    val session: Session,
    val sets: List<Set>
)

data class RoutineWithDetails(
    val routine: eric.bitria.minimalfit.data.entity.gym.Routine,
    val exerciseCount: Int,
    val setCount: Int
)

class GymViewModel(
    gymSessionManager: GymSessionManager
) : ViewModel() {

    val hasActiveWorkout: StateFlow<Boolean> = gymSessionManager.activeSession
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
}
