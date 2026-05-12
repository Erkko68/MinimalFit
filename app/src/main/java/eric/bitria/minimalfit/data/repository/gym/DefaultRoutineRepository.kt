package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.RoutineDao
import eric.bitria.minimalfit.data.entity.gym.Routine
import kotlinx.coroutines.flow.Flow

class DefaultRoutineRepository(
    private val dao: RoutineDao
) : RoutineRepository {

    override fun getAll(): Flow<List<Routine>> = dao.getAll()

    override fun getById(id: String): Flow<Routine?> = dao.getById(id)

    override suspend fun add(routine: Routine) = dao.insert(routine)

    override suspend fun update(routine: Routine) = dao.update(routine)

    override suspend fun delete(id: String) = dao.delete(id)
}
