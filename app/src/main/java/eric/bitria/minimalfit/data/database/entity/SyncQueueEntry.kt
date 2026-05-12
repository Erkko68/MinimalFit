package eric.bitria.minimalfit.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import eric.bitria.minimalfit.util.nowInstant
import kotlin.time.Instant

/**
 * Tracks pending local changes that need to be synced to Firestore.
 * Entries are enqueued by repositories after each mutating operation and
 * flushed by [eric.bitria.minimalfit.data.remote.sync.SyncScheduler].
 */
@Entity(
    tableName = "sync_queue",
    indices = [Index(value = ["entityType", "entityId"])]
)
data class SyncQueueEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,   // "exercise", "routine", "session", "ingredient", "meal", "diet", "meal_log", "track"
    val entityId: String,
    val action: String,       // "upsert" or "delete"
    val createdAt: Instant = nowInstant()
)
