package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.ExerciseDao
import eric.bitria.minimalfit.data.entity.gym.Exercise
import kotlinx.coroutines.flow.Flow

class DefaultExerciseRepository(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    override fun getExercises(): Flow<List<Exercise>> =
        exerciseDao.getExercises()

    override suspend fun getExerciseById(exerciseId: String): Exercise? =
        exerciseDao.getExerciseById(exerciseId)

    override suspend fun addExercise(name: String): Exercise {
        val exercise = Exercise(name = name)
        exerciseDao.insertExercise(exercise)
        return exercise
    }

    override suspend fun updateExerciseRest(exerciseId: String, restSeconds: Int) {
        val exercise = exerciseDao.getExerciseById(exerciseId) ?: return
        exerciseDao.updateExercise(exercise.copy(restSeconds = restSeconds.coerceAtLeast(0)))
    }

    override suspend fun deleteExercise(exerciseId: String) {
        exerciseDao.deleteExercise(exerciseId)
    }
}

