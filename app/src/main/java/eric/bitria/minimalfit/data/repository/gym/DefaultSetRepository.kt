package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.SetDao
import eric.bitria.minimalfit.data.entity.gym.Set
import kotlinx.coroutines.flow.Flow

class DefaultSetRepository(
    private val setDao: SetDao
) : SetRepository {

    override fun getSetsForExercise(exerciseId: String): Flow<List<Set>> =
        setDao.getSetsForExercise(exerciseId)

    override fun getSetsForSession(sessionId: String): Flow<List<Set>> =
        setDao.getSetsForSession(sessionId)

    override fun getSet(id: String): Flow<Set?> =
        setDao.getSet(id)

    override suspend fun addSet(set: Set, sessionId: String) {
        // Ensure the set is linked to the correct session
        setDao.insertSet(set.copy(sessionId = sessionId))
    }

    override suspend fun updateSet(set: Set) {
        setDao.updateSet(set)
    }

    override suspend fun deleteSet(id: String) {
        setDao.deleteSet(id)
    }
}
