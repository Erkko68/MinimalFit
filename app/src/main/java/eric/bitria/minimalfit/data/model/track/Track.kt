package eric.bitria.minimalfit.data.model.track

import java.time.LocalDate
import java.time.LocalTime
import kotlin.time.Duration

data class Track(
    val id: String,
    val date: LocalDate,
    val time: LocalTime,
    val name: String,
    val distance: Double, // in km
    val duration: Duration,
    val pace: String, // e.g., "5:30 min/km"
    val routePoints: List<TrackPoint> = emptyList()
)
