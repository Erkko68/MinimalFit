package eric.bitria.minimalfit.data.gym

import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.SessionExercise
import eric.bitria.minimalfit.data.entity.gym.Set as GymSet
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.SessionExerciseRepository
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class GymTrackingLogic(
    private val sessionRepository: SessionRepository,
    private val exerciseRepository: ExerciseRepository,
    private val setRepository: SetRepository,
    private val sessionExerciseRepository: SessionExerciseRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _activeSession = MutableStateFlow<Session?>(null)
    val activeSession: StateFlow<Session?> = _activeSession.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeSessionExercises: StateFlow<List<SessionExercise>> = _activeSession
        .flatMapLatest { session ->
            if (session == null) flowOf(emptyList())
            else sessionExerciseRepository.getSessionExercises(session.id)
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

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

    // Elapsed when we last paused (or the stored duration when loading a past session)
    private var elapsedAtPause: Duration = Duration.ZERO
    // Wall-clock millis when we last resumed (null when paused)
    private var resumeWallMillis: Long = 0L

    fun start() {
        scope.launch {
            if (_activeSession.value != null) return@launch
            val sessionId = sessionRepository.startSession()
            sessionRepository.getSession(sessionId).first()?.let { session ->
                elapsedAtPause = Duration.ZERO
                resumeWallMillis = System.currentTimeMillis()
                _elapsed.value = Duration.ZERO
                _isPaused.value = false
                _activeSession.value = session
                startTicker()
            }
        }
    }

    fun loadSession(sessionId: String) {
        scope.launch {
            sessionRepository.getSession(sessionId).first()?.let { session ->
                tickerJob?.cancel()
                stopRestInternal()
                elapsedAtPause = session.durationSeconds.seconds
                _elapsed.value = elapsedAtPause
                resumeWallMillis = 0L
                _isPaused.value = true
                _activeSession.value = session
            }
        }
    }

    fun pause() {
        if (_activeSession.value == null || _isPaused.value) return
        elapsedAtPause = _elapsed.value
        _isPaused.value = true
        tickerJob?.cancel()
    }

    fun resume() {
        if (_activeSession.value == null || !_isPaused.value) return
        elapsedAtPause = _elapsed.value
        resumeWallMillis = System.currentTimeMillis()
        _isPaused.value = false
        startTicker()
    }

    fun addExercise(exerciseId: String) {
        scope.launch {
            val session = _activeSession.value ?: return@launch
            val sessionExercise = SessionExercise(sessionId = session.id, exerciseId = exerciseId)
            sessionExerciseRepository.add(sessionExercise)
            setRepository.addSet(
                GymSet(
                    sessionExerciseId = sessionExercise.id,
                    sessionId = session.id,
                    weight = 0f,
                    reps = 0
                )
            )
        }
    }

    fun addSet(sessionExerciseId: String) {
        scope.launch {
            val session = _activeSession.value ?: return@launch
            setRepository.addSet(
                GymSet(
                    sessionExerciseId = sessionExerciseId,
                    sessionId = session.id,
                    weight = 0f,
                    reps = 0
                )
            )
        }
    }

    fun updateSet(set: GymSet) {
        scope.launch {
            val previous = setRepository.getSet(set.id).first()
            setRepository.updateSet(set)

            if (previous?.isCompleted == false && set.isCompleted) {
                val sessionExercise = activeSessionExercises.value
                    .firstOrNull { it.id == set.sessionExerciseId }
                    ?: return@launch
                val exercise = exerciseRepository.getExercise(sessionExercise.exerciseId).first()
                startRest(exercise?.restSeconds ?: 60)
            }
        }
    }

    fun deleteSet(setId: String) {
        scope.launch { setRepository.deleteSet(setId) }
    }

    fun deleteExercise(sessionExerciseId: String) {
        scope.launch {
            setRepository.deleteSetsForSessionExercise(sessionExerciseId)
            sessionExerciseRepository.delete(sessionExerciseId)
        }
    }

    fun finish() {
        val session = _activeSession.value ?: return
        val finalElapsed = _elapsed.value
        tickerJob?.cancel()
        stopRestInternal()
        _activeSession.value = null
        _elapsed.value = Duration.ZERO
        _isPaused.value = false
        elapsedAtPause = Duration.ZERO
        resumeWallMillis = 0L
        scope.launch {
            sessionRepository.finishSession(session.id, finalElapsed.inWholeSeconds)
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

    fun updateSessionTitle(title: String) {
        scope.launch {
            val session = _activeSession.value ?: return@launch
            val updated = session.copy(title = title)
            sessionRepository.updateSession(updated)
            _activeSession.value = updated
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (true) {
                val added = (System.currentTimeMillis() - resumeWallMillis).milliseconds
                _elapsed.value = elapsedAtPause + added
                delay(500)
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
        if (leftMillis == 0L) stopRestInternal()
    }

    private fun stopRestInternal() {
        restJob?.cancel()
        restJob = null
        restEndEpochMillis = null
        _restRemaining.value = Duration.ZERO
        _isRestRunning.value = false
    }
}
