package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eric.bitria.minimalfit.data.entity.gym.Session
import eric.bitria.minimalfit.data.entity.gym.SessionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

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
}

