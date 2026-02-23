package eric.bitria.minimalfit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavDestination(
    val route: Route,
    val label: String,
    val icon: ImageVector,
) {
    HOME(Route.Home, "Home", Icons.Default.Home),
    SETTINGS(Route.Settings, "Settings", Icons.Default.Settings),
}