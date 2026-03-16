package eric.bitria.minimalfit.data.model.track

import eric.bitria.minimalfit.util.nowInstant
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Instant = nowInstant()
)
