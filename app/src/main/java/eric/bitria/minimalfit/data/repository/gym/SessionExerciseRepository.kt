package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.entity.gym.SessionExercise
import kotlinx.coroutines.flow.Flow

interface SessionExerciseRepository {
    fun getSessionExercises(sessionId: String): Flow<List<SessionExercise>>
    suspend fun add(sessionExercise: SessionExercise)
    suspend fun delete(id: String)
    suspend fun deleteForSession(sessionId: String)
}
