package eric.bitria.minimalfit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.ui.graphics.vector.ImageVector

enum class QuickAction(
    val route: Route,
    val label: String,
    val icon: ImageVector,
) {
    LOG_ACTIVITY(Route.IndoorActivities, "Log Activity", Icons.Default.FitnessCenter),
    LOG_MEAL(Route.Food, "Log Meal", Icons.Default.RestaurantMenu),
    LOG_ROUTE(Route.OutdoorActivities, "Log Route", Icons.AutoMirrored.Filled.DirectionsWalk)
}
