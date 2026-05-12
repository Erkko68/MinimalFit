package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.ExerciseDao
import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.data.remote.sync.SyncScheduler
import kotlinx.coroutines.flow.Flow

class DefaultExerciseRepository(
    private val exerciseDao: ExerciseDao,
    private val syncScheduler: SyncScheduler
) : ExerciseRepository {

    override fun getExercises(query: String, limit: Int): Flow<List<Exercise>> =
        if (query.isBlank()) exerciseDao.getExercises(query, limit)
        else exerciseDao.getExercises(query, limit)

    override fun getExercise(id: String): Flow<Exercise?> =
        exerciseDao.getExercise(id)

    override suspend fun addExercise(exercise: Exercise) {
        exerciseDao.insertExercise(exercise)
        if (!exercise.isGlobal) syncScheduler.enqueue("exercise", exercise.id, "upsert")
    }

    override suspend fun updateExercise(exercise: Exercise) {
        exerciseDao.updateExercise(exercise)
        if (!exercise.isGlobal) syncScheduler.enqueue("exercise", exercise.id, "upsert")
    }

    override suspend fun deleteExercise(id: String) {
        exerciseDao.deleteExercise(id)
        syncScheduler.enqueue("exercise", id, "delete")
    }
}
