package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eric.bitria.minimalfit.data.entity.track.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY date DESC, time DESC LIMIT :limit")
    fun getTracks(limit: Int): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE name LIKE '%' || :query || '%' ORDER BY date DESC, time DESC LIMIT :limit")
    fun getTracks(query: String, limit: Int): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE date BETWEEN :start AND :end ORDER BY date DESC, time DESC")
    fun getTracks(start: LocalDate, end: LocalDate): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE id = :id")
    fun getTrack(id: String): Flow<Track?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: Track)

    @Update
    suspend fun updateTrack(track: Track)

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrack(id: String)
}
