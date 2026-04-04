package eric.bitria.minimalfit.data.gym

import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.SessionStatus
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.SessionRepository
import eric.bitria.minimalfit.data.repository.gym.SetRepository
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
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class GymTrackingLogic(
    private val sessionRepository: SessionRepository,
    private val setRepository: SetRepository,
    private val exerciseRepository: ExerciseRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _activeSession = MutableStateFlow<Session?>(null)
    val activeSession: StateFlow<Session?> = _activeSession.asStateFlow()

    private val _elapsed = MutableStateFlow(Duration.ZERO)
    val elapsed: StateFlow<Duration> = _elapsed.asStateFlow()

    private val _restRemaining = MutableStateFlow(Duration.ZERO)
    val restRemaining: StateFlow<Duration> = _restRemaining.asStateFlow()

    private val _isRestRunning = MutableStateFlow(false)
    val isRestRunning: StateFlow<Boolean> = _isRestRunning.asStateFlow()

    private val _hasIncompleteSet = MutableStateFlow(false)
    val hasIncompleteSet: StateFlow<Boolean> = _hasIncompleteSet.asStateFlow()

    private var tickerJob: Job? = null
    private var restJob: Job? = null
    private var setObserverJob: Job? = null
    private var restEndEpochMillis: Long? = null

    init {
        scope.launch {
            sessionRepository.getActiveSession().collect { session ->
                _activeSession.value = session
                syncTicker(session)
                syncIncompleteSetObserver(session)
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
            stopRestInternal()
        }
    }

    fun startRestForExercise(exerciseId: String) {
        scope.launch {
            val exercise = exerciseRepository.getExerciseById(exerciseId)
            val seconds = (exercise?.restSeconds ?: 120).coerceAtLeast(0)
            startRestCountdown(seconds)
        }
    }

    fun addRestSeconds(seconds: Int) {
        if (seconds <= 0) return
        val currentEnd = restEndEpochMillis ?: return
        restEndEpochMillis = currentEnd + (seconds * 1000L)
        syncRestTick()
    }

    fun stopRest() {
        stopRestInternal()
    }

    fun finishLatestSetAndStartRest() {
        scope.launch {
            val sessionId = _activeSession.value?.id ?: return@launch
            val completedSet = setRepository.completeLatestIncompleteSet(sessionId) ?: return@launch
            val exercise = exerciseRepository.getExerciseById(completedSet.exerciseId)
            val seconds = (exercise?.restSeconds ?: 120).coerceAtLeast(0)
            startRestCountdown(seconds)
        }
    }

    fun updateExerciseRest(exerciseId: String, restSeconds: Int) {
        scope.launch {
            exerciseRepository.updateExerciseRest(exerciseId, restSeconds)
        }
    }

    private fun syncTicker(session: Session?) {
        tickerJob?.cancel()

        if (session == null) {
            _elapsed.value = Duration.ZERO
            stopRestInternal()
            return
        }

        if (session.status != SessionStatus.ACTIVE) {
            _elapsed.value = calculateElapsed(session)
            return
        }

        tickerJob = scope.launch {
            while (true) {
                _elapsed.value = calculateElapsed(session)
                delay(1000)
            }
        }
    }

    private fun syncIncompleteSetObserver(session: Session?) {
        setObserverJob?.cancel()

        if (session == null || session.status == SessionStatus.COMPLETED) {
            _hasIncompleteSet.value = false
            return
        }

        setObserverJob = scope.launch {
            setRepository.getSetsForSession(session.id)
                .collect { sets ->
                    _hasIncompleteSet.value = sets.any { !it.isCompleted }
                }
        }
    }

    private fun calculateElapsed(session: Session): Duration {
        val endReference = when {
            session.status == SessionStatus.PAUSED && session.pausedAt != null -> session.pausedAt
            session.status == SessionStatus.COMPLETED && session.endTime != null -> session.endTime
            else -> Clock.System.now()
        }

        val raw = endReference - session.startTime
        return (raw - session.pausedDurationSeconds.seconds).coerceAtLeast(Duration.ZERO)
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

