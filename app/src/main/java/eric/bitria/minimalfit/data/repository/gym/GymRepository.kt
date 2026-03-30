package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.GymSessionWithSets
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.entity.gym.GymSetWithSession
import kotlinx.coroutines.flow.Flow

interface GymRepository {
    fun getRecentSessions(limit: Int = 20): Flow<List<Session>>
    fun getRecentSessionsWithSets(limit: Int = 20): Flow<List<GymSessionWithSets>>
    fun getSession(sessionId: String): Flow<GymSessionWithSets?>
    fun getActiveSession(): Flow<GymSessionWithSets?>

    suspend fun startSession(): String
    suspend fun pauseSession()
    suspend fun resumeSession()
    suspend fun finishSession()

    fun getExercises(): Flow<List<Exercise>>
    fun getSetsForExercise(exerciseId: String): Flow<List<Set>>
    fun getSetsWithSessionForExercise(exerciseId: String): Flow<List<GymSetWithSession>>

    suspend fun addExercise(name: String): Exercise
    suspend fun deleteExercise(exerciseId: String)

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
    suspend fun deleteSet(setId: String)

    suspend fun deleteSession(sessionId: String)

    suspend fun copyPreviousSet(sessionId: String, exerciseId: String): Set?
}
