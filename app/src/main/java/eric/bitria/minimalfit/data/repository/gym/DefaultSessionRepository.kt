package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.GymDao
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.SessionStatus
import eric.bitria.minimalfit.util.nowInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class DefaultSessionRepository(
    private val gymDao: GymDao
) : SessionRepository {

    override fun getRecentSessions(limit: Int): Flow<List<Session>> =
        gymDao.getRecentSessions(limit)

    override fun getSession(sessionId: String): Flow<Session?> =
        gymDao.getSession(sessionId)

    override fun getActiveSession(): Flow<Session?> =
        gymDao.getActiveSession()

    override suspend fun startSession(): String {
        val session = Session(
            startTime = nowInstant(),
            status = SessionStatus.ACTIVE
        )
        gymDao.insertSession(session)
        return session.id
    }

    override suspend fun pauseSession() {
        val active = gymDao.getActiveSession().first() ?: return
        gymDao.updateSession(active.copy(status = SessionStatus.PAUSED))
    }

    override suspend fun resumeSession() {
        val active = gymDao.getActiveSession().first() ?: return
        gymDao.updateSession(active.copy(status = SessionStatus.ACTIVE))
    }

    override suspend fun finishSession() {
        val active = gymDao.getActiveSession().first() ?: return
        gymDao.updateSession(active.copy(status = SessionStatus.COMPLETED, endTime = nowInstant()))
    }

    override suspend fun deleteSession(sessionId: String) {
        gymDao.deleteSessionAndSets(sessionId)
    }
}

