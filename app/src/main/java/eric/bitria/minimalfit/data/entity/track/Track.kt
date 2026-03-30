package eric.bitria.minimalfit.data.entity.track

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import java.util.UUID

@Serializable
@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val startTime: Instant,
    val endTime: Instant,
    val name: String,
    val distance: Double, // in km
    val pace: String, // e.g., "5:30 min/km"
    val routePoints: List<TrackPoint> = emptyList()
)
