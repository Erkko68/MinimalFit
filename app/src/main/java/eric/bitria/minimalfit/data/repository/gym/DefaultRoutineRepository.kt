package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.RoutineDao
import eric.bitria.minimalfit.data.entity.gym.Routine
import eric.bitria.minimalfit.data.remote.sync.SyncScheduler
import kotlinx.coroutines.flow.Flow

class DefaultRoutineRepository(
    private val dao: RoutineDao,
    private val syncScheduler: SyncScheduler
) : RoutineRepository {

    override fun getAll(): Flow<List<Routine>> = dao.getAll()

    override fun getById(id: String): Flow<Routine?> = dao.getById(id)

    override suspend fun add(routine: Routine) {
        dao.insert(routine)
        syncScheduler.enqueue("routine", routine.id, "upsert")
    }

    override suspend fun update(routine: Routine) {
        dao.update(routine)
        syncScheduler.enqueue("routine", routine.id, "upsert")
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
        syncScheduler.enqueue("routine", id, "delete")
    }
}
