package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.SetDao
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.remote.sync.SyncScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class DefaultSetRepository(
    private val setDao: SetDao,
    private val syncScheduler: SyncScheduler
) : SetRepository {

    override fun getAllSets(): Flow<List<Set>> =
        setDao.getAllSets()

    override fun getSetsForExercise(exerciseId: String): Flow<List<Set>> =
        setDao.getSetsForExercise(exerciseId)

    override fun getSetsForSessionExercise(sessionExerciseId: String): Flow<List<Set>> =
        setDao.getSetsForSessionExercise(sessionExerciseId)

    override fun getSetsForSession(sessionId: String): Flow<List<Set>> =
        setDao.getSetsForSession(sessionId)

    override fun getSet(id: String): Flow<Set?> =
        setDao.getSet(id)

    override suspend fun addSet(set: Set) {
        setDao.insertSet(set)
        syncScheduler.enqueue("session", set.sessionId, "upsert")
    }

    override suspend fun updateSet(set: Set) {
        setDao.updateSet(set)
        syncScheduler.enqueue("session", set.sessionId, "upsert")
    }

    override suspend fun deleteSet(id: String) {
        val sessionId = setDao.getSet(id).firstOrNull()?.sessionId
        setDao.deleteSet(id)
        if (sessionId != null) syncScheduler.enqueue("session", sessionId, "upsert")
    }

    override suspend fun deleteSetsForSessionExercise(sessionExerciseId: String) =
        setDao.deleteSetsForSessionExercise(sessionExerciseId)
}
