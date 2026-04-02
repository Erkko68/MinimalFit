package eric.bitria.minimalfit.data.gym

import eric.bitria.minimalfit.data.entity.gym.Session
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

interface GymSessionManager {
    val activeSession: StateFlow<Session?>
    val elapsed: StateFlow<Duration>

    fun start()
    fun pause()
    fun resume()
    fun finish()
}

