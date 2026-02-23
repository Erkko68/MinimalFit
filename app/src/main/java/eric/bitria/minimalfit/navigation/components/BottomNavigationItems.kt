package eric.bitria.minimalfit.navigation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import eric.bitria.minimalfit.navigation.Screens

sealed class BottomNavigationItems(val route: Any, val label: String, val icon: ImageVector) {

    data object Home : BottomNavigationItems(
        route = Screens.Home,
        label = "Home",
        icon = Icons.Default.Home
    )

    data object Settings : BottomNavigationItems(
        route = Screens.Settings,
        label = "Settings",
        icon = Icons.Default.Settings
    )
}
