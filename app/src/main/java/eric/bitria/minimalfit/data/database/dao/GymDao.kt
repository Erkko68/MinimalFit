package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import eric.bitria.minimalfit.data.entity.gym.GymExerciseEntity
import eric.bitria.minimalfit.data.entity.gym.GymSessionEntity
import eric.bitria.minimalfit.data.entity.gym.GymSessionStatus
import eric.bitria.minimalfit.data.entity.gym.GymSessionWithSets
import eric.bitria.minimalfit.data.entity.gym.GymSetEntity
import eric.bitria.minimalfit.data.entity.gym.GymSetWithSession
import kotlinx.coroutines.flow.Flow

@Dao
interface GymDao {

    // Exercises catalog
    @Query("SELECT * FROM gym_exercises ORDER BY name ASC")
    fun getExercises(): Flow<List<GymExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: GymExerciseEntity)

    @Query("DELETE FROM gym_exercises WHERE id = :exerciseId")
    suspend fun deleteExercise(exerciseId: String)

    // Sessions
    @Query("SELECT * FROM gym_sessions ORDER BY date DESC, startTime DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<GymSessionEntity>>

    @Query("SELECT * FROM gym_sessions WHERE id = :id")
    fun getSession(id: String): Flow<GymSessionEntity?>

    @Query("SELECT * FROM gym_sessions WHERE status IN (:activeStatuses) ORDER BY date DESC, startTime DESC LIMIT 1")
    fun getActiveSession(activeStatuses: List<GymSessionStatus> = listOf(GymSessionStatus.ACTIVE, GymSessionStatus.PAUSED)): Flow<GymSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GymSessionEntity)

    @Update
    suspend fun updateSession(session: GymSessionEntity)

    // Sets
    @Transaction
    @Query("SELECT * FROM gym_sets WHERE exerciseId = :exerciseId")
    fun getSetsWithSessionForExercise(exerciseId: String): Flow<List<GymSetWithSession>>

    @Query("SELECT gym_sets.* FROM gym_sets INNER JOIN gym_sessions ON gym_sets.sessionId = gym_sessions.id WHERE gym_sets.exerciseId = :exerciseId ORDER BY gym_sessions.date ASC, gym_sessions.startTime ASC")
    fun getSetsForExercise(exerciseId: String): Flow<List<GymSetEntity>>

    @Query("SELECT * FROM gym_sets WHERE sessionId = :sessionId ORDER BY orderInSession ASC")
    fun getSetsForSession(sessionId: String): Flow<List<GymSetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: GymSetEntity)

    @Update
    suspend fun updateSet(set: GymSetEntity)

    @Query("DELETE FROM gym_sets WHERE id = :setId")
    suspend fun deleteSet(setId: String)

    @Query("DELETE FROM gym_sets WHERE sessionId = :sessionId")
    suspend fun deleteSetsForSession(sessionId: String)

    @Query("DELETE FROM gym_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Transaction
    suspend fun deleteSessionAndSets(sessionId: String) {
        deleteSetsForSession(sessionId)
        deleteSession(sessionId)
    }

    @Transaction
    @Query("SELECT * FROM gym_sessions WHERE id = :sessionId")
    fun getSessionWithSets(sessionId: String): Flow<GymSessionWithSets?>

    @Transaction
    @Query("SELECT * FROM gym_sessions ORDER BY date DESC, startTime DESC LIMIT :limit")
    fun getRecentSessionsWithSets(limit: Int): Flow<List<GymSessionWithSets>>
}
