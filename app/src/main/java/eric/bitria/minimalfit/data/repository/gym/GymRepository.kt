package eric.bitria.minimalfit.data.repository.gym

import eric.bitria.minimalfit.data.entity.gym.GymExerciseEntity
import eric.bitria.minimalfit.data.entity.gym.GymSessionEntity
import eric.bitria.minimalfit.data.entity.gym.GymSessionWithSets
import eric.bitria.minimalfit.data.entity.gym.GymSetEntity
import kotlinx.coroutines.flow.Flow

interface GymRepository {
    fun getRecentSessions(limit: Int = 20): Flow<List<GymSessionEntity>>
    fun getRecentSessionsWithSets(limit: Int = 20): Flow<List<GymSessionWithSets>>
    fun getSession(sessionId: String): Flow<GymSessionWithSets?>
    fun getActiveSession(): Flow<GymSessionWithSets?>

    suspend fun startSession(): String
    suspend fun pauseSession()
    suspend fun resumeSession()
    suspend fun finishSession()

    fun getExercises(): Flow<List<GymExerciseEntity>>
    suspend fun addExercise(name: String): GymExerciseEntity

    suspend fun addSet(
        sessionId: String,
        exerciseId: String,
        weight: Float,
        reps: Int,
        rpe: Float? = null,
        isWarmup: Boolean = false,
        notes: String = ""
    )

    suspend fun updateSet(set: GymSetEntity)
    suspend fun deleteSet(setId: String)

    suspend fun deleteSession(sessionId: String)

    suspend fun copyPreviousSet(sessionId: String, exerciseId: String): GymSetEntity?
}
