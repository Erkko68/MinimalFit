package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eric.bitria.minimalfit.data.entity.track.Track
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

@Dao
interface TrackDao {
    @Query("""
        SELECT * FROM tracks 
        WHERE (name LIKE '%' || :query || '%')
        AND (:start IS NULL OR startTime >= :start)
        AND (:end IS NULL OR startTime <= :end)
        ORDER BY startTime DESC 
        LIMIT :limit
    """)
    fun getTracks(
        query: String = "",
        start: Instant? = null,
        end: Instant? = null,
        limit: Int = -1
    ): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE id = :id")
    fun getTrack(id: String): Flow<Track?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: Track)

    @Update
    suspend fun updateTrack(track: Track)

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrack(id: String)
}
