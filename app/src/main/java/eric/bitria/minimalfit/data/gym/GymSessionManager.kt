package eric.bitria.minimalfit.data.gym

import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.SessionExercise
import eric.bitria.minimalfit.data.entity.gym.Set
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

interface GymSessionManager {
    val activeSessionExercises: StateFlow<List<SessionExercise>>
    val activeSets: StateFlow<List<Set>>
    val elapsed: StateFlow<Duration>
    val restRemaining: StateFlow<Duration>
    val isRestRunning: StateFlow<Boolean>
    val isPaused: StateFlow<Boolean>
    val activeSession: StateFlow<Session?>

    fun start()
    fun startFromRoutine(exercises: List<RoutineExercisePlan>, routineName: String)
    fun replaceWithRoutine(exercises: List<RoutineExercisePlan>, routineName: String)
    fun loadSession(sessionId: String)
    fun finish()
    fun addExercise(exerciseId: String)
    fun addSet(sessionExerciseId: String, weight: Float, reps: Int, isCompleted: Boolean = false)
    fun updateSet(set: Set)
    fun deleteSet(setId: String)
    fun deleteExercise(sessionExerciseId: String)

    fun startRest(seconds: Int = 60)
    fun stopRest()
    fun pause()
    fun resume()
    fun updateSessionTitle(title: String)
}
