package eric.bitria.minimalfit.navigation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector
import eric.bitria.minimalfit.navigation.Screens

sealed class QuickActionItems(
    val route: Any,
    val label: String,
    val icon: ImageVector
) {
    data object AddWorkout : QuickActionItems(
        Screens.Home,
        "Add Workout",
        Icons.Default.FitnessCenter
    )

    data object AddMeal : QuickActionItems(
        Screens.Home,
        "Add Meal",
        Icons.Default.Restaurant
    )

    data object LogWeight : QuickActionItems(
        Screens.Home,
        "Log Weight",
        Icons.Default.MonitorWeight
    )
}