package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.SessionExerciseDao
import eric.bitria.minimalfit.data.entity.gym.SessionExercise
import eric.bitria.minimalfit.data.remote.sync.SyncScheduler
import kotlinx.coroutines.flow.Flow

class DefaultSessionExerciseRepository(
    private val dao: SessionExerciseDao,
    private val syncScheduler: SyncScheduler
) : SessionExerciseRepository {

    override fun getAllSessionExercises(): Flow<List<SessionExercise>> =
        dao.getAllSessionExercises()

    override fun getSessionExercises(sessionId: String): Flow<List<SessionExercise>> =
        dao.getSessionExercises(sessionId)

    override suspend fun add(sessionExercise: SessionExercise) {
        dao.insert(sessionExercise)
        syncScheduler.enqueue("session", sessionExercise.sessionId, "upsert")
    }

    override suspend fun delete(id: String) {
        val sessionId = dao.getSessionIdForExercise(id)
        dao.delete(id)
        if (sessionId != null) syncScheduler.enqueue("session", sessionId, "upsert")
    }

    override suspend fun deleteForSession(sessionId: String) =
        dao.deleteForSession(sessionId)
}
