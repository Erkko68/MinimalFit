package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.entity.gym.RoutineSet
import kotlinx.coroutines.flow.Flow

interface RoutineSetRepository {
    fun getForRoutineExercise(routineExerciseId: String): Flow<List<RoutineSet>>
    fun getForRoutine(routineId: String): Flow<List<RoutineSet>>
    suspend fun add(routineSet: RoutineSet)
    suspend fun update(routineSet: RoutineSet)
    suspend fun delete(id: String)
    suspend fun deleteForRoutineExercise(routineExerciseId: String)
}
