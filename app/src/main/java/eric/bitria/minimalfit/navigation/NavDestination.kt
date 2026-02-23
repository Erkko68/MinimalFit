package eric.bitria.minimalfit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavDestination(
    val route: Route,
    val label: String,
    val icon: ImageVector,
) {
    FOOD(Route.Food, "Food", Icons.Default.RestaurantMenu),
    PROFILE(Route.Profile, "Me", Icons.Default.Person),
    INDOOR_ACTIVITIES(Route.IndoorActivities, "Workout", Icons.Default.FitnessCenter),
    OUTDOOR_ACTIVITIES(Route.OutdoorActivities, "Track", Icons.AutoMirrored.Filled.DirectionsWalk)
}