package eric.bitria.minimalfit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector

enum class QuickAction(
    val route: Route,
    val label: String,
    val icon: ImageVector,
) {
    ADD_WORKOUT(Route.Home, "Add Workout", Icons.Default.FitnessCenter),
    ADD_MEAL(Route.Home, "Add Meal", Icons.Default.Restaurant),
    LOG_WEIGHT(Route.Home, "Log Weight", Icons.Default.MonitorWeight),
}
