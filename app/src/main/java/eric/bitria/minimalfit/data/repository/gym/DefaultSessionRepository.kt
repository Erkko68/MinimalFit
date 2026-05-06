package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.SessionDao
import eric.bitria.minimalfit.data.database.dao.SetDao
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.util.nowInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.Instant

class DefaultSessionRepository(
    private val sessionDao: SessionDao,
    private val setDao: SetDao
) : SessionRepository {

    override fun getSessions(query: String, limit: Int): Flow<List<Session>> =
        sessionDao.getSessions(query, limit)

    override fun getSessions(start: Instant, end: Instant): Flow<List<Session>> =
        sessionDao.getSessions(start, end)

    override fun getSession(id: String): Flow<Session?> =
        sessionDao.getSession(id)

    override fun getActiveSession(): Flow<Session?> =
        sessionDao.getActiveSession()

    override suspend fun addSession(session: Session) {
        sessionDao.insertSession(session)
    }

    override suspend fun updateSession(session: Session) {
        sessionDao.updateSession(session)
    }

    override suspend fun deleteSession(id: String) {
        setDao.deleteSetsForSession(id)
        sessionDao.deleteSession(id)
    }

    override suspend fun startSession(): String {
        val session = Session(
            startTime = nowInstant(),
            isCompleted = false
        )
        sessionDao.insertSession(session)
        return session.id
    }

    override suspend fun finishSession(id: String, durationSeconds: Long) {
        val session = sessionDao.getSession(id).firstOrNull() ?: return
        sessionDao.updateSession(
            session.copy(
                isCompleted = true,
                durationSeconds = durationSeconds
            )
        )
    }
}
