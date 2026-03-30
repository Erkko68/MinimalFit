package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.entity.gym.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getRecentSessions(limit: Int = 20): Flow<List<Session>>
    fun getSession(sessionId: String): Flow<Session?>
    fun getActiveSession(): Flow<Session?>

    suspend fun startSession(): String
    suspend fun pauseSession()
    suspend fun resumeSession()
    suspend fun finishSession()
    suspend fun deleteSession(sessionId: String)
}

