package eric.bitria.minimalfit.data.gym

import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.Set as GymSet
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.SessionRepository
import eric.bitria.minimalfit.data.repository.gym.SetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class GymTrackingLogic(
    private val sessionRepository: SessionRepository,
    private val exerciseRepository: ExerciseRepository,
    private val setRepository: SetRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _activeSession = MutableStateFlow<Session?>(null)
    val activeSession: StateFlow<Session?> = _activeSession.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeSets: StateFlow<List<GymSet>> = _activeSession
        .flatMapLatest { session ->
            if (session == null) flowOf(emptyList())
            else setRepository.getSetsForSession(session.id)
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _elapsed = MutableStateFlow(Duration.ZERO)
    val elapsed: StateFlow<Duration> = _elapsed.asStateFlow()

    private val _restRemaining = MutableStateFlow(Duration.ZERO)
    val restRemaining: StateFlow<Duration> = _restRemaining.asStateFlow()

    private val _isRestRunning = MutableStateFlow(false)
    val isRestRunning: StateFlow<Boolean> = _isRestRunning.asStateFlow()

    private var tickerJob: Job? = null
    private var restJob: Job? = null
    private var restEndEpochMillis: Long? = null

    fun start() {
        scope.launch {
            if (_activeSession.value == null) {
                val sessionId = sessionRepository.startSession()
                sessionRepository.getSession(sessionId).first()?.let {
                    _activeSession.value = it
                    syncTicker(it)
                }
            }
        }
    }

    fun addSet(exerciseId: String) {
        scope.launch {
            val session = _activeSession.value ?: return@launch
            setRepository.addSet(
                GymSet(
                    sessionId = session.id,
                    exerciseId = exerciseId,
                    weight = 0f,
                    reps = 0
                ),
                session.id
            )
        }
    }

    fun updateSet(set: GymSet) {
        scope.launch {
            setRepository.updateSet(set)
        }
    }

    fun deleteSet(setId: String) {
        scope.launch {
            setRepository.deleteSet(setId)
        }
    }

    fun finish() {
        scope.launch {
            val session = _activeSession.value ?: return@launch
            sessionRepository.finishSession(session.id, _elapsed.value.inWholeSeconds)
            _activeSession.value = null
            _elapsed.value = Duration.ZERO
            stopRestInternal()
        }
    }

    fun startRest(seconds: Int = 60) {
        if (seconds <= 0) return
        val currentEnd = restEndEpochMillis
        if (currentEnd == null || !_isRestRunning.value) {
            startRestCountdown(seconds)
        } else {
            restEndEpochMillis = currentEnd + (seconds * 1000L)
            syncRestTick()
        }
    }

    fun stopRest() {
        stopRestInternal()
    }

    private fun syncTicker(session: Session) {
        tickerJob?.cancel()

        tickerJob = scope.launch {
            while (true) {
                val now = Clock.System.now()
                _elapsed.value = (now - session.startTime).coerceAtLeast(Duration.ZERO)
                delay(1000)
            }
        }
    }

    private fun startRestCountdown(seconds: Int) {
        stopRestInternal()
        if (seconds <= 0) return

        _isRestRunning.value = true
        restEndEpochMillis = System.currentTimeMillis() + seconds * 1000L
        restJob = scope.launch {
            while (true) {
                syncRestTick()
                if (!_isRestRunning.value) break
                delay(250)
            }
        }
    }

    private fun syncRestTick() {
        val end = restEndEpochMillis ?: return
        val leftMillis = (end - System.currentTimeMillis()).coerceAtLeast(0L)
        _restRemaining.value = leftMillis.milliseconds
        if (leftMillis == 0L) {
            stopRestInternal()
        }
    }

    private fun stopRestInternal() {
        restJob?.cancel()
        restJob = null
        restEndEpochMillis = null
        _restRemaining.value = Duration.ZERO
        _isRestRunning.value = false
    }
}
