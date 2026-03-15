package eric.bitria.minimalfit.navigation

import kotlinx.serialization.Serializable

sealed class Route {
    @Serializable data object Profile : Route()
    @Serializable data object Settings : Route()
    @Serializable data object Food : Route()
    @Serializable data object OutdoorActivities : Route()
    @Serializable data object IndoorActivities : Route()
    @Serializable data object TrackRecording : Route()
    @Serializable data class DailyLog(val date: String, val openSearch: Boolean = false) : Route()
    @Serializable data class TrackDetail(val trackId: String) : Route()
    @Serializable data class DietDetail(val dietId: Int) : Route()
}
