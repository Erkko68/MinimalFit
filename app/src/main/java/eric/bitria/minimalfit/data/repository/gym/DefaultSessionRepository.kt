package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.SessionDao
import eric.bitria.minimalfit.data.database.dao.SetDao
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.SessionStatus
import eric.bitria.minimalfit.util.nowInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class DefaultSessionRepository(
    private val sessionDao: SessionDao,
    private val setDao: SetDao
) : SessionRepository {

    override fun getRecentSessions(limit: Int): Flow<List<Session>> =
        sessionDao.getRecentSessions(limit)

    override fun getSession(sessionId: String): Flow<Session?> =
        sessionDao.getSession(sessionId)

    override fun getActiveSession(): Flow<Session?> =
        sessionDao.getActiveSession()

    override suspend fun startSession(): String {
        val session = Session(
            startTime = nowInstant(),
            status = SessionStatus.ACTIVE
        )
        sessionDao.insertSession(session)
        return session.id
    }

    override suspend fun pauseSession() {
        val active = sessionDao.getActiveSession().first() ?: return
        sessionDao.updateSession(active.copy(status = SessionStatus.PAUSED))
    }

    override suspend fun resumeSession() {
        val active = sessionDao.getActiveSession().first() ?: return
        sessionDao.updateSession(active.copy(status = SessionStatus.ACTIVE))
    }

    override suspend fun finishSession() {
        val active = sessionDao.getActiveSession().first() ?: return
        sessionDao.updateSession(active.copy(status = SessionStatus.COMPLETED, endTime = nowInstant()))
    }

    override suspend fun deleteSession(sessionId: String) {
        setDao.deleteSessionAndSets(sessionId)
    }
}

