package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.RoutineExerciseDao
import eric.bitria.minimalfit.data.entity.gym.RoutineExercise
import kotlinx.coroutines.flow.Flow

class DefaultRoutineExerciseRepository(
    private val dao: RoutineExerciseDao
) : RoutineExerciseRepository {

    override fun getForRoutine(routineId: String): Flow<List<RoutineExercise>> =
        dao.getForRoutine(routineId)

    override suspend fun add(routineExercise: RoutineExercise) = dao.insert(routineExercise)

    override suspend fun delete(id: String) = dao.delete(id)

    override suspend fun deleteForRoutine(routineId: String) = dao.deleteForRoutine(routineId)
}
