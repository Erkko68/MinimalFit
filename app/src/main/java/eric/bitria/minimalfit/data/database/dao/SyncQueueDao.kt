package eric.bitria.minimalfit.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import eric.bitria.minimalfit.data.database.entity.SyncQueueEntry

@Dao
interface SyncQueueDao {

    @Insert
    suspend fun enqueue(entry: SyncQueueEntry)

    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    suspend fun getAll(): List<SyncQueueEntry>

    @Query("DELETE FROM sync_queue WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM sync_queue")
    suspend fun clearAll()
}
