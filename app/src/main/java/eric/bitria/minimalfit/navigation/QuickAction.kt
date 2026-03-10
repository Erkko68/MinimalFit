package eric.bitria.minimalfit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

enum class QuickAction(
    val label: String,
    val icon: ImageVector
) {
    START_TRACK("Start Track", Icons.AutoMirrored.Filled.DirectionsRun),
    ADD_MEAL("Register Today's Meal", Icons.Default.Restaurant),
    REGISTER_WORKOUT("Register Workout", Icons.Default.FitnessCenter),
    START_TIMER("Start Timer", Icons.Default.Timer)
}