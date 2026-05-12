package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.SessionDao
import eric.bitria.minimalfit.data.database.dao.SetDao
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.util.nowInstant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import eric.bitria.minimalfit.util.shortMonthDay
import eric.bitria.minimalfit.util.hourMinute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.Instant

class DefaultSessionRepository(
    private val sessionDao: SessionDao,
    private val setDao: SetDao
) : SessionRepository {

    override fun getSessions(
        query: String,
        start: Instant?,
        end: Instant?,
        limit: Int
    ): Flow<List<Session>> = sessionDao.getSessions(query, start, end, limit)

    override fun getSession(id: String): Flow<Session?> =
        sessionDao.getSession(id)

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
        val start = nowInstant()
        val localStart = start.toLocalDateTime(TimeZone.currentSystemDefault())
        val title = "${localStart.date.shortMonthDay()} • ${localStart.time.hourMinute()}"
        val session = Session(
            startTime = start,
            title = title
        )
        sessionDao.insertSession(session)
        return session.id
    }

    override suspend fun finishSession(id: String, durationSeconds: Long) {
        val session = sessionDao.getSession(id).firstOrNull() ?: return
        sessionDao.updateSession(session.copy(durationSeconds = durationSeconds, isFinished = true))
    }
}
