package eric.bitria.minimalfit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector

enum class QuickAction(
    val label: String,
    val icon: ImageVector
) {
    ADD_MEAL("Register Meal", Icons.Default.Restaurant),
    START_WORKOUT("Start Workout", Icons.AutoMirrored.Filled.DirectionsRun)
}