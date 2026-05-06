package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.ExerciseDao
import eric.bitria.minimalfit.data.entity.gym.Exercise
import kotlinx.coroutines.flow.Flow

class DefaultExerciseRepository(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    override fun getExercises(query: String, limit: Int): Flow<List<Exercise>> =
        if (query.isBlank()) exerciseDao.getExercises(query,limit)
        else exerciseDao.getExercises(query, limit)

    override fun getExercise(id: String): Flow<Exercise?> =
        exerciseDao.getExercise(id)

    override suspend fun addExercise(exercise: Exercise) {
        exerciseDao.insertExercise(exercise)
    }

    override suspend fun updateExercise(exercise: Exercise) {
        exerciseDao.updateExercise(exercise)
    }

    override suspend fun deleteExercise(id: String) {
        exerciseDao.deleteExercise(id)
    }
}
