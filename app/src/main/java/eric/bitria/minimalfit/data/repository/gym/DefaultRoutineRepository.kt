package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.RoutineDao
import eric.bitria.minimalfit.data.entity.gym.RoutineSummary
import kotlinx.coroutines.flow.Flow

class DefaultRoutineRepository(
    private val routineDao: RoutineDao
) : RoutineRepository {

    override fun getRoutines(): Flow<List<RoutineSummary>> =
        routineDao.getRoutineSummaries()

    override suspend fun getRoutineExerciseIds(id: String): List<String> =
        routineDao.getRoutineExerciseIds(id)

    override suspend fun createRoutine(name: String, exerciseIds: List<String>) {
        routineDao.createRoutine(name.trim(), exerciseIds)
    }

    override suspend fun deleteRoutine(id: String) {
        routineDao.deleteRoutineWithExercises(id)
    }
}
