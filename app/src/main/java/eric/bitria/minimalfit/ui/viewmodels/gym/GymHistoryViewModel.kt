package eric.bitria.minimalfit.ui.viewmodels.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.repository.gym.SessionRepository
import eric.bitria.minimalfit.data.repository.gym.SetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GymHistoryViewModel(
    private val sessionRepository: SessionRepository,
    private val setRepository: SetRepository
) : ViewModel() {

    val pastSessions: StateFlow<List<SessionWithSets>> = sessionRepository
        .getSessions(limit = 20)
        .flatMapLatest { sessions ->
            if (sessions.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    sessions.map { session ->
                        setRepository.getSetsForSession(session.id).map { sets ->
                            SessionWithSets(session, sets)
                        }
                    }
                ) { it.toList() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteSession(sessionId: String) {
        viewModelScope.launch { sessionRepository.deleteSession(sessionId) }
    }
}
