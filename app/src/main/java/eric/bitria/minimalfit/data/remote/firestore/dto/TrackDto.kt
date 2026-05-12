package eric.bitria.minimalfit.data.remote.firestore.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrackPointDto(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: String = ""
)

@Serializable
data class TrackDocument(
    val id: String = "",
    val startTime: String = "",
    val durationMillis: Long = 0L,
    val name: String = "",
    val distance: Double = 0.0,
    val pace: String = "",
    val routePoints: List<TrackPointDto> = emptyList(),
    val updatedAt: String = ""
)
