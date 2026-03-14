package eric.bitria.minimalfit.data.model

import java.time.Instant

data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Instant = Instant.now()
)

