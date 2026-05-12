package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.RoutineSetDao
import eric.bitria.minimalfit.data.entity.gym.RoutineSet
import eric.bitria.minimalfit.data.remote.sync.SyncScheduler
import kotlinx.coroutines.flow.Flow

class DefaultRoutineSetRepository(
    private val dao: RoutineSetDao,
    private val syncScheduler: SyncScheduler
) : RoutineSetRepository {

    override fun getForRoutineExercise(routineExerciseId: String): Flow<List<RoutineSet>> =
        dao.getForRoutineExercise(routineExerciseId)

    override fun getForRoutine(routineId: String): Flow<List<RoutineSet>> =
        dao.getForRoutine(routineId)

    override suspend fun add(routineSet: RoutineSet) {
        dao.insert(routineSet)
        val routineId = dao.getRoutineIdByRoutineExerciseId(routineSet.routineExerciseId)
        if (routineId != null) syncScheduler.enqueue("routine", routineId, "upsert")
    }

    override suspend fun update(routineSet: RoutineSet) {
        dao.update(routineSet)
        val routineId = dao.getRoutineIdByRoutineExerciseId(routineSet.routineExerciseId)
        if (routineId != null) syncScheduler.enqueue("routine", routineId, "upsert")
    }

    override suspend fun delete(id: String) {
        val routineId = dao.getRoutineIdForSet(id)
        dao.delete(id)
        if (routineId != null) syncScheduler.enqueue("routine", routineId, "upsert")
    }

    override suspend fun deleteForRoutineExercise(routineExerciseId: String) =
        dao.deleteForRoutineExercise(routineExerciseId)
}
