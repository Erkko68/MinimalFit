package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.RoutineDao
import eric.bitria.minimalfit.data.entity.gym.Routine
import eric.bitria.minimalfit.data.entity.gym.RoutineExerciseCrossRef
import eric.bitria.minimalfit.data.entity.gym.RoutineExerciseTarget
import eric.bitria.minimalfit.data.entity.gym.RoutineSummary
import kotlinx.coroutines.flow.Flow

class DefaultRoutineRepository(
    private val routineDao: RoutineDao
) : RoutineRepository {

    override fun getRoutineEntities(): Flow<List<Routine>> =
        routineDao.getAllRoutines()

    override fun getRoutines(): Flow<List<RoutineSummary>> =
        routineDao.getRoutineSummaries()

    override fun getRoutineExercises(): Flow<List<RoutineExerciseCrossRef>> =
        routineDao.getAllRoutineExercises()

    override suspend fun getRoutineExerciseIds(id: String): List<String> =
        routineDao.getRoutineExerciseIds(id)

    override suspend fun getRoutineExercises(id: String): List<RoutineExerciseCrossRef> =
        routineDao.getRoutineExercises(id)

    override suspend fun createRoutine(name: String, exerciseIds: List<String>) {
        routineDao.createRoutine(name.trim(), exerciseIds)
    }

    override suspend fun createRoutineWithTargets(
        name: String,
        exerciseTargets: List<RoutineExerciseTarget>
    ) {
        routineDao.createRoutineWithTargets(name.trim(), exerciseTargets)
    }

    override suspend fun renameRoutine(id: String, name: String) {
        routineDao.renameRoutine(id, name.trim())
    }

    override suspend fun deleteRoutine(id: String) {
        routineDao.deleteRoutineWithExercises(id)
    }
}
