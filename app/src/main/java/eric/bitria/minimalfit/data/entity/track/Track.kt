package eric.bitria.minimalfit.data.entity.track

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalTime
import java.util.UUID
import kotlin.time.Duration

@Serializable
@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val time: LocalTime,
    val name: String,
    val distance: Double, // in km
    val duration: Duration,
    val pace: String, // e.g., "5:30 min/km"
    val routePoints: List<TrackPoint> = emptyList()
)
