package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eric.bitria.minimalfit.data.entity.gym.SessionExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionExerciseDao {

    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun getSessionExercises(sessionId: String): Flow<List<SessionExercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sessionExercise: SessionExercise)

    @Query("DELETE FROM session_exercises WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM session_exercises WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: String)
}
