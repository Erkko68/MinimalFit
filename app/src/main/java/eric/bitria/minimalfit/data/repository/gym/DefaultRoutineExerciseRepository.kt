package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.RoutineExerciseDao
import eric.bitria.minimalfit.data.entity.gym.RoutineExercise
import eric.bitria.minimalfit.data.remote.sync.SyncScheduler
import kotlinx.coroutines.flow.Flow

class DefaultRoutineExerciseRepository(
    private val dao: RoutineExerciseDao,
    private val syncScheduler: SyncScheduler
) : RoutineExerciseRepository {

    override fun getForRoutine(routineId: String): Flow<List<RoutineExercise>> =
        dao.getForRoutine(routineId)

    override suspend fun add(routineExercise: RoutineExercise) {
        dao.insert(routineExercise)
        syncScheduler.enqueue("routine", routineExercise.routineId, "upsert")
    }

    override suspend fun delete(id: String) {
        val routineId = dao.getRoutineIdForExercise(id)
        dao.delete(id)
        if (routineId != null) syncScheduler.enqueue("routine", routineId, "upsert")
    }

    override suspend fun deleteForRoutine(routineId: String) = dao.deleteForRoutine(routineId)
}
