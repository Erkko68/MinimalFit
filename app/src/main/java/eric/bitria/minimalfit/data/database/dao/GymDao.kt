package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import eric.bitria.minimalfit.data.entity.gym.Exercise
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.SessionStatus
import eric.bitria.minimalfit.data.entity.gym.GymSessionWithSets
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.data.entity.gym.GymSetWithSession
import kotlinx.coroutines.flow.Flow

@Dao
interface GymDao {

    // Exercises catalog
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getExercises(): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Query("DELETE FROM exercises WHERE id = :exerciseId")
    suspend fun deleteExercise(exerciseId: String)

    // Sessions
    @Query("SELECT * FROM sessions ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    fun getSession(id: String): Flow<Session?>

    @Query("SELECT * FROM sessions WHERE status IN (:activeStatuses) ORDER BY startTime DESC LIMIT 1")
    fun getActiveSession(activeStatuses: List<SessionStatus> = listOf(SessionStatus.ACTIVE, SessionStatus.PAUSED)): Flow<Session?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session)

    @Update
    suspend fun updateSession(session: Session)

    // Sets
    @Transaction
    @Query("SELECT * FROM sets WHERE exerciseId = :exerciseId")
    fun getSetsWithSessionForExercise(exerciseId: String): Flow<List<GymSetWithSession>>

    @Query("SELECT sets.* FROM sets INNER JOIN sessions ON sets.sessionId = sessions.id WHERE sets.exerciseId = :exerciseId ORDER BY sessions.startTime ASC")
    fun getSetsForExercise(exerciseId: String): Flow<List<Set>>

    @Query("SELECT * FROM sets WHERE sessionId = :sessionId ORDER BY orderInSession ASC")
    fun getSetsForSession(sessionId: String): Flow<List<Set>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: Set)

    @Update
    suspend fun updateSet(set: Set)

    @Query("DELETE FROM sets WHERE id = :setId")
    suspend fun deleteSet(setId: String)

    @Query("DELETE FROM sets WHERE sessionId = :sessionId")
    suspend fun deleteSetsForSession(sessionId: String)

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Transaction
    suspend fun deleteSessionAndSets(sessionId: String) {
        deleteSetsForSession(sessionId)
        deleteSession(sessionId)
    }

    @Transaction
    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    fun getSessionWithSets(sessionId: String): Flow<GymSessionWithSets?>

    @Transaction
    @Query("SELECT * FROM sessions ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSessionsWithSets(limit: Int): Flow<List<GymSessionWithSets>>
}
