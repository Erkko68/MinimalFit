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
        if (active.status != SessionStatus.ACTIVE) return
        sessionDao.updateSession(
            active.copy(
                status = SessionStatus.PAUSED,
                pausedAt = nowInstant()
            )
        )
    }

    override suspend fun resumeSession() {
        val active = sessionDao.getActiveSession().first() ?: return
        if (active.status != SessionStatus.PAUSED) return

        val pausedAt = active.pausedAt ?: nowInstant()
        val pauseDeltaSeconds = (nowInstant() - pausedAt).inWholeSeconds.coerceAtLeast(0)
        sessionDao.updateSession(
            active.copy(
                status = SessionStatus.ACTIVE,
                pausedAt = null,
                pausedDurationSeconds = active.pausedDurationSeconds + pauseDeltaSeconds
            )
        )
    }

    override suspend fun finishSession() {
        val active = sessionDao.getActiveSession().first() ?: return
        val now = nowInstant()
        val extraPaused = if (active.status == SessionStatus.PAUSED && active.pausedAt != null) {
            (now - active.pausedAt).inWholeSeconds.coerceAtLeast(0)
        } else {
            0L
        }
        sessionDao.updateSession(
            active.copy(
                status = SessionStatus.COMPLETED,
                endTime = now,
                pausedAt = null,
                pausedDurationSeconds = active.pausedDurationSeconds + extraPaused
            )
        )
    }

    override suspend fun deleteSession(sessionId: String) {
        setDao.deleteSessionAndSets(sessionId)
    }
}

