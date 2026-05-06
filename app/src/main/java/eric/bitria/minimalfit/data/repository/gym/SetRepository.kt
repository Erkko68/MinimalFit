package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.Set
import kotlinx.coroutines.flow.Flow

/**
 * Repository for accessing exercise sets.
 */
interface SetRepository {

    /** Returns all sets for a specific exercise across all sessions. */
    fun getSetsForExercise(exerciseId: String): Flow<List<Set>>

    /** Returns all sets for a specific session. */
    fun getSetsForSession(sessionId: String): Flow<List<Set>>

    /** Returns a specific set by ID. */
    fun getSet(id: String): Flow<Set?>

    /** Adds a new set to a session. */
    suspend fun addSet(set: Set, sessionId: String)

    /** Updates an existing set. */
    suspend fun updateSet(set: Set)

    /** Deletes a set by ID. */
    suspend fun deleteSet(id: String)
}
