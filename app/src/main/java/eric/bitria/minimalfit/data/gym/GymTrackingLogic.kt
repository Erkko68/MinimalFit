package eric.bitria.minimalfit.data.gym

import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.SessionExercise
import eric.bitria.minimalfit.data.entity.gym.SetType
import eric.bitria.minimalfit.data.entity.gym.Set as GymSet
import eric.bitria.minimalfit.data.remote.fcm.FcmRepository
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
    private val sessionExerciseRepository: SessionExerciseRepository,
    private val fcmRepository: FcmRepository,
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
            startSessionInternal()
        }
    }

    fun startFromRoutine(exercises: List<RoutineExercisePlan>, routineName: String) {
        scope.launch {
            if (_activeSession.value != null) return@launch
            val session = startSessionInternal(routineName) ?: return@launch
            exercises.distinctBy { it.exerciseId }.forEach { plan ->
                addExerciseToSession(session, plan)
            }
        }
    }

    fun replaceWithRoutine(exercises: List<RoutineExercisePlan>, routineName: String) {
        scope.launch {
            val currentSession = _activeSession.value
            tickerJob?.cancel()
            stopRestInternal()
            if (currentSession != null) {
                sessionExerciseRepository.deleteForSession(currentSession.id)
                sessionRepository.deleteSession(currentSession.id)
            }
            val session = createSessionInternal(routineName) ?: return@launch
            exercises.distinctBy { it.exerciseId }.forEach { plan ->
                addExerciseToSession(session, plan)
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
            addExerciseToSession(session, RoutineExercisePlan(exerciseId = exerciseId, targetSets = 0))
        }
    }

    fun addSet(sessionExerciseId: String, weight: Float, reps: Int, isCompleted: Boolean = false) {
        scope.launch {
            val session = _activeSession.value ?: return@launch
            setRepository.addSet(
                GymSet(
                    sessionExerciseId = sessionExerciseId,
                    sessionId = session.id,
                    weight = weight,
                    reps = reps,
                    isCompleted = isCompleted
                )
            )
        }
    }

    fun addTimedSet(
        sessionExerciseId: String,
        weight: Float,
        durationSeconds: Int,
        preparationSeconds: Int = 5,
        isCompleted: Boolean = false
    ) {
        scope.launch {
            val session = _activeSession.value ?: return@launch
            setRepository.addSet(
                GymSet(
                    sessionExerciseId = sessionExerciseId,
                    sessionId = session.id,
                    weight = weight,
                    reps = 0,
                    type = SetType.Timed,
                    durationSeconds = durationSeconds.coerceAtLeast(1),
                    preparationSeconds = preparationSeconds.coerceAtLeast(0),
                    isCompleted = isCompleted
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
                restartRest(exercise?.restSeconds ?: 60)
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
            val totalKg = setRepository.getTodayTotalWeightKg()
            if (totalKg >= 1000.0) {
                runCatching { fcmRepository.checkWeightMilestone(totalKg) }
            }
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

    fun discard() {
        scope.launch {
            discardActiveSession()
        }
    }

    private fun restartRest(seconds: Int) {
        stopRestInternal()
        startRestCountdown(seconds)
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

    private suspend fun startSessionInternal(title: String? = null): Session? {
        if (_activeSession.value != null) return _activeSession.value
        return createSessionInternal(title)
    }

    private suspend fun createSessionInternal(title: String? = null): Session? {
        val sessionId = sessionRepository.startSession()
        val session = sessionRepository.getSession(sessionId).first() ?: return null
        val titledSession = title
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { routineTitle ->
                session.copy(title = routineTitle).also { sessionRepository.updateSession(it) }
            }
            ?: session
        elapsedAtPause = Duration.ZERO
        resumeWallMillis = System.currentTimeMillis()
        _elapsed.value = Duration.ZERO
        _isPaused.value = false
        _activeSession.value = titledSession
        startTicker()
        return titledSession
    }

    private suspend fun discardActiveSession() {
        val session = _activeSession.value ?: return
        tickerJob?.cancel()
        stopRestInternal()
        _activeSession.value = null
        _elapsed.value = Duration.ZERO
        _isPaused.value = false
        elapsedAtPause = Duration.ZERO
        resumeWallMillis = 0L
        sessionExerciseRepository.deleteForSession(session.id)
        sessionRepository.deleteSession(session.id)
    }

    private suspend fun addExerciseToSession(session: Session, plan: RoutineExercisePlan) {
        val sessionExercise = SessionExercise(sessionId = session.id, exerciseId = plan.exerciseId)
        sessionExerciseRepository.add(sessionExercise)
        val plannedSets = plan.plannedSets.ifEmpty {
            List(plan.targetSets.coerceAtLeast(0)) {
                PlannedSetPlan(
                    weight = plan.targetWeight,
                    reps = plan.targetReps,
                    type = plan.targetType,
                    durationSeconds = plan.targetDurationSeconds,
                    preparationSeconds = plan.targetPreparationSeconds
                )
            }
        }
        plannedSets.forEach { plannedSet ->
            setRepository.addSet(
                GymSet(
                    sessionExerciseId = sessionExercise.id,
                    sessionId = session.id,
                    weight = plannedSet.weight.coerceAtLeast(0f),
                    reps = plannedSet.reps.coerceAtLeast(0),
                    type = plannedSet.type,
                    durationSeconds = plannedSet.durationSeconds.coerceAtLeast(0),
                    preparationSeconds = plannedSet.preparationSeconds.coerceAtLeast(0)
                )
            )
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        var ticksSinceLastSave = 0
        tickerJob = scope.launch {
            while (true) {
                val added = (System.currentTimeMillis() - resumeWallMillis).milliseconds
                _elapsed.value = elapsedAtPause + added
                // Persist elapsed every ~30 s so the duration isn't fully lost if the process is killed.
                if (++ticksSinceLastSave >= 60) {
                    ticksSinceLastSave = 0
                    _activeSession.value?.let { session ->
                        sessionRepository.updateSession(session.copy(durationSeconds = _elapsed.value.inWholeSeconds))
                    }
                }
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
