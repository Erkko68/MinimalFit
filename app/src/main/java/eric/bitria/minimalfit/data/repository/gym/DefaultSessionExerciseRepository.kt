package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.SessionExerciseDao
import eric.bitria.minimalfit.data.entity.gym.SessionExercise
import kotlinx.coroutines.flow.Flow

class DefaultSessionExerciseRepository(
    private val dao: SessionExerciseDao
) : SessionExerciseRepository {

    override fun getSessionExercises(sessionId: String): Flow<List<SessionExercise>> =
        dao.getSessionExercises(sessionId)

    override suspend fun add(sessionExercise: SessionExercise) =
        dao.insert(sessionExercise)

    override suspend fun delete(id: String) =
        dao.delete(id)

    override suspend fun deleteForSession(sessionId: String) =
        dao.deleteForSession(sessionId)
}
