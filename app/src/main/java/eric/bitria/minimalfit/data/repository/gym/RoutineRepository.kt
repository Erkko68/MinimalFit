package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.entity.gym.Routine
import eric.bitria.minimalfit.data.entity.gym.RoutineExerciseCrossRef
import eric.bitria.minimalfit.data.entity.gym.RoutineExerciseTarget
import eric.bitria.minimalfit.data.entity.gym.RoutineSummary
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun getRoutineEntities(): Flow<List<Routine>>
    fun getRoutines(): Flow<List<RoutineSummary>>
    fun getRoutineExercises(): Flow<List<RoutineExerciseCrossRef>>
    suspend fun getRoutineExerciseIds(id: String): List<String>
    suspend fun getRoutineExercises(id: String): List<RoutineExerciseCrossRef>
    suspend fun createRoutine(name: String, exerciseIds: List<String>)
    suspend fun createRoutineWithTargets(name: String, exerciseTargets: List<RoutineExerciseTarget>)
    suspend fun renameRoutine(id: String, name: String)
    suspend fun deleteRoutine(id: String)
}
