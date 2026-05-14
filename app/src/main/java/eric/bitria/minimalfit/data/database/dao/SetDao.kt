package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eric.bitria.minimalfit.data.entity.gym.Set
import kotlinx.coroutines.flow.Flow

@Dao
interface SetDao {

    @Query("SELECT * FROM sets ORDER BY sessionId, createdAt ASC")
    fun getAllSets(): Flow<List<Set>>

    @Query("""
        SELECT s.* FROM sets s
        JOIN session_exercises se ON s.sessionExerciseId = se.id
        WHERE se.exerciseId = :exerciseId
        ORDER BY s.createdAt ASC
    """)
    fun getSetsForExercise(exerciseId: String): Flow<List<Set>>

    @Query("SELECT * FROM sets WHERE sessionExerciseId = :sessionExerciseId ORDER BY createdAt ASC")
    fun getSetsForSessionExercise(sessionExerciseId: String): Flow<List<Set>>

    @Query("SELECT * FROM sets WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun getSetsForSession(sessionId: String): Flow<List<Set>>

    @Query("SELECT * FROM sets WHERE id = :id LIMIT 1")
    fun getSet(id: String): Flow<Set?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: Set)

    @Update
    suspend fun updateSet(set: Set)

    @Query("DELETE FROM sets WHERE id = :id")
    suspend fun deleteSet(id: String)

    @Query("DELETE FROM sets WHERE sessionExerciseId = :sessionExerciseId")
    suspend fun deleteSetsForSessionExercise(sessionExerciseId: String)

    @Query("DELETE FROM sets WHERE sessionId = :sessionId")
    suspend fun deleteSetsForSession(sessionId: String)

    @Query("SELECT COALESCE(SUM(weight * reps), 0.0) FROM sets WHERE createdAt >= :startMs AND createdAt < :endMs")
    suspend fun getTodayTotalWeightKg(startMs: Long, endMs: Long): Double
}
