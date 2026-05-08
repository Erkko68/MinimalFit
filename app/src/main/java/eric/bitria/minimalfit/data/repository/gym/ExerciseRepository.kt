package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.entity.gym.Exercise
import kotlinx.coroutines.flow.Flow

/**
 * Repository for accessing gym exercises.
 */
interface ExerciseRepository {

    /** Returns exercises matching the query or all if query is empty.
     *  Default limit = -1 means return all results.
     */
    fun getExercises(query: String = "", limit: Int = -1): Flow<List<Exercise>>

    /** Returns a specific exercise by ID. */
    fun getExercise(id: String): Flow<Exercise?>

    /** Adds a new exercise. */
    suspend fun addExercise(exercise: Exercise)

    /** Updates an existing exercise. */
    suspend fun updateExercise(exercise: Exercise)

    /** Deletes an exercise by ID. */
    suspend fun deleteExercise(id: String)
}
