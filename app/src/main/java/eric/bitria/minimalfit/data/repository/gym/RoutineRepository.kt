package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.entity.gym.RoutineSummary
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun getRoutines(): Flow<List<RoutineSummary>>
    suspend fun getRoutineExerciseIds(id: String): List<String>
    suspend fun createRoutine(name: String, exerciseIds: List<String>)
    suspend fun renameRoutine(id: String, name: String)
    suspend fun deleteRoutine(id: String)
}
