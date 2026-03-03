package eric.bitria.minimalfit.navigation

import kotlinx.serialization.Serializable

sealed class Route {
    @Serializable data object Profile : Route()
    @Serializable data object Settings : Route()
    @Serializable data object Food : Route()
    @Serializable data class MealSelection(val date: String) : Route()
    @Serializable data object FoodSearch : Route()
    @Serializable data object MealConfig : Route()
    @Serializable data object OutdoorActivities : Route()
    @Serializable data object IndoorActivities : Route()
}