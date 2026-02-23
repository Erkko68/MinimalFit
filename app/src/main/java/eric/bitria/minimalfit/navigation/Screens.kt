package eric.bitria.minimalfit.navigation

import kotlinx.serialization.Serializable

sealed class Route {
    @Serializable data object Home : Route()
    @Serializable data object Settings : Route()
}