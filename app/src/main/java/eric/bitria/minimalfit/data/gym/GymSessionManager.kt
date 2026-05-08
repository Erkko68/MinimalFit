package eric.bitria.minimalfit.data.gym

import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.Set
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

interface GymSessionManager {
    val activeSets: StateFlow<List<Set>>
    val elapsed: StateFlow<Duration>
    val restRemaining: StateFlow<Duration>
    val isRestRunning: StateFlow<Boolean>
    val isPaused: StateFlow<Boolean>
    val activeSession: StateFlow<Session?>

    fun start()
    fun finish()
    fun addSet(exerciseId: String)
    fun updateSet(set: Set)
    fun deleteSet(setId: String)
    
    fun startRest(seconds: Int = 60)
    fun stopRest()
    fun pause()
    fun resume()
}
