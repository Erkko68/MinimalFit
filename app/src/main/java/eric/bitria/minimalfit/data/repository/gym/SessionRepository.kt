package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.entity.gym.Session
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

/**
 * Repository for accessing gym sessions.
 */
interface SessionRepository {

    /** Returns sessions matching the query or all if query is empty. */
    fun getSessions(query: String = "", limit: Int = 20): Flow<List<Session>>

    /** Returns sessions within a specific time range. */
    fun getSessions(start: Instant, end: Instant): Flow<List<Session>>

    /** Returns a specific session by ID. */
    fun getSession(id: String): Flow<Session?>

    /** Returns the currently active session if any. */
    fun getActiveSession(): Flow<Session?>

    /** Adds a new session. */
    suspend fun addSession(session: Session)

    /** Updates an existing session. */
    suspend fun updateSession(session: Session)

    /** Deletes a session by ID. */
    suspend fun deleteSession(id: String)

    // --- Session Lifecycle Management ---
    suspend fun startSession(): String
    suspend fun finishSession(id: String, durationSeconds: Long)
}
