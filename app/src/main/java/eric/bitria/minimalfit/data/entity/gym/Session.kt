package eric.bitria.minimalfit.data.entity.gym

import androidx.room.Entity
import androidx.room.PrimaryKey
import eric.bitria.minimalfit.data.entity.gym.SessionStatus
import kotlin.time.Instant
import java.util.UUID

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val startTime: Instant,
    val endTime: Instant? = null,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val notes: String = ""
)

