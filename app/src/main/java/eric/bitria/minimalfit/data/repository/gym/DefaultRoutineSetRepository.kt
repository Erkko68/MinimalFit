package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.RoutineSetDao
import eric.bitria.minimalfit.data.entity.gym.RoutineSet
import kotlinx.coroutines.flow.Flow

class DefaultRoutineSetRepository(
    private val dao: RoutineSetDao
) : RoutineSetRepository {

    override fun getForRoutineExercise(routineExerciseId: String): Flow<List<RoutineSet>> =
        dao.getForRoutineExercise(routineExerciseId)

    override fun getForRoutine(routineId: String): Flow<List<RoutineSet>> =
        dao.getForRoutine(routineId)

    override suspend fun add(routineSet: RoutineSet) = dao.insert(routineSet)

    override suspend fun update(routineSet: RoutineSet) = dao.update(routineSet)

    override suspend fun delete(id: String) = dao.delete(id)

    override suspend fun deleteForRoutineExercise(routineExerciseId: String) =
        dao.deleteForRoutineExercise(routineExerciseId)
}
