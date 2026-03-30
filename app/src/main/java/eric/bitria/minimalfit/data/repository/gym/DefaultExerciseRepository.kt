package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.database.dao.GymDao
import eric.bitria.minimalfit.data.entity.gym.Exercise
import kotlinx.coroutines.flow.Flow

class DefaultExerciseRepository(
    private val gymDao: GymDao
) : ExerciseRepository {

    override fun getExercises(): Flow<List<Exercise>> =
        gymDao.getExercises()

    override suspend fun addExercise(name: String): Exercise {
        val exercise = Exercise(name = name)
        gymDao.insertExercise(exercise)
        return exercise
    }

    override suspend fun deleteExercise(exerciseId: String) {
        gymDao.deleteExercise(exerciseId)
    }
}

