package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eric.bitria.minimalfit.data.entity.gym.Session
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

@Dao
interface SessionDao {

    @Query("""
        SELECT * FROM sessions 
        WHERE (notes LIKE '%' || :query || '%')
        AND (:start IS NULL OR startTime >= :start)
        AND (:end IS NULL OR startTime <= :end)
        ORDER BY startTime DESC 
        LIMIT :limit
    """)
    fun getSessions(
        query: String = "",
        start: Instant? = null,
        end: Instant? = null,
        limit: Int = -1
    ): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    fun getSession(id: String): Flow<Session?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session)

    @Update
    suspend fun updateSession(session: Session)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteSession(id: String)
}
