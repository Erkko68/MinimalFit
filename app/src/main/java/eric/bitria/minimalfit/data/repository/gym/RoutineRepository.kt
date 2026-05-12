package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.entity.gym.Routine
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun getAll(): Flow<List<Routine>>
    fun getById(id: String): Flow<Routine?>
    suspend fun add(routine: Routine)
    suspend fun update(routine: Routine)
    suspend fun delete(id: String)
}
