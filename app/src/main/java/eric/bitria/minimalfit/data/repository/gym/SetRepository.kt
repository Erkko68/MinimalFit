package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.entity.gym.Set
import kotlinx.coroutines.flow.Flow

interface SetRepository {
    fun getSetsForExercise(exerciseId: String): Flow<List<Set>>
    fun getSetsForSessionExercise(sessionExerciseId: String): Flow<List<Set>>
    fun getSetsForSession(sessionId: String): Flow<List<Set>>
    fun getSet(id: String): Flow<Set?>
    suspend fun addSet(set: Set)
    suspend fun updateSet(set: Set)
    suspend fun deleteSet(id: String)
    suspend fun deleteSetsForSessionExercise(sessionExerciseId: String)
}
