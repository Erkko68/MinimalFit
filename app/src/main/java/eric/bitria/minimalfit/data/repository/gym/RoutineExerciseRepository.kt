package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.entity.gym.RoutineExercise
import kotlinx.coroutines.flow.Flow

interface RoutineExerciseRepository {
    fun getForRoutine(routineId: String): Flow<List<RoutineExercise>>
    suspend fun add(routineExercise: RoutineExercise)
    suspend fun delete(id: String)
    suspend fun deleteForRoutine(routineId: String)
}
