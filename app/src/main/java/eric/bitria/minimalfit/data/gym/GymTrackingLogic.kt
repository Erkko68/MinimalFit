package eric.bitria.minimalfit.data.gym

import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.SessionStatus
import eric.bitria.minimalfit.data.repository.gym.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration

class GymTrackingLogic(
    private val sessionRepository: SessionRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _activeSession = MutableStateFlow<Session?>(null)
    val activeSession: StateFlow<Session?> = _activeSession.asStateFlow()

    private val _elapsed = MutableStateFlow(Duration.ZERO)
    val elapsed: StateFlow<Duration> = _elapsed.asStateFlow()

    private var tickerJob: Job? = null

    init {
        scope.launch {
            sessionRepository.getActiveSession().collect { session ->
                _activeSession.value = session
                syncTicker(session)
            }
        }
    }

    fun start() {
        scope.launch {
            val current = sessionRepository.getActiveSession().first()
            when (current?.status) {
                null -> sessionRepository.startSession()
                SessionStatus.PAUSED -> sessionRepository.resumeSession()
                else -> Unit
            }
        }
    }

    fun pause() {
        scope.launch { sessionRepository.pauseSession() }
    }

    fun resume() {
        scope.launch { sessionRepository.resumeSession() }
    }

    fun finish() {
        scope.launch {
            sessionRepository.finishSession()
            _elapsed.value = Duration.ZERO
        }
    }

    private fun syncTicker(session: Session?) {
        tickerJob?.cancel()

        if (session == null) {
            _elapsed.value = Duration.ZERO
            return
        }

        if (session.status != SessionStatus.ACTIVE) {
            _elapsed.value = Clock.System.now() - session.startTime
            return
        }

        tickerJob = scope.launch {
            while (true) {
                _elapsed.value = Clock.System.now() - session.startTime
                delay(1000)
            }
        }
    }
}

