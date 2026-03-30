package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.entity.gym.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getExercises(): Flow<List<Exercise>>

    suspend fun addExercise(name: String): Exercise
    suspend fun deleteExercise(exerciseId: String)
}

