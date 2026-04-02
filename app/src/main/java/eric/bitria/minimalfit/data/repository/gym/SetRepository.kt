package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.Set
import kotlinx.coroutines.flow.Flow

interface SetRepository {
    fun getSetsForExercise(exerciseId: String): Flow<List<Set>>
    fun getSetsForSession(sessionId: String): Flow<List<Set>>
    fun getSessionForSet(setId: String): Flow<Session?>
    suspend fun getSetById(setId: String): Set?

    suspend fun addSet(
        sessionId: String,
        exerciseId: String,
        weight: Float,
        reps: Int,
        rpe: Float? = null,
        isWarmup: Boolean = false,
        notes: String = ""
    )

    suspend fun updateSet(set: Set)
    suspend fun completeLatestIncompleteSet(sessionId: String): Set?
    suspend fun deleteSet(setId: String)
    suspend fun copyPreviousSet(sessionId: String, exerciseId: String): Set?
}

