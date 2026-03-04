package eric.bitria.minimalfit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController

/**
 * Defines all app-level quick actions surfaced via the global FAB.
 *
 * Each entry carries:
 * - [label]      — human-readable action name shown next to the action button
 * - [icon]       — icon displayed on the action button
 * - [navigate]   — executes the navigation given the available nav controllers
 *
 * Navigation is self-contained here so [eric.bitria.minimalfit.navigation.composables.QuickActionFab] needs no routing logic,
 * and the host composable only needs to supply the nav controllers.
 */
enum class QuickAction(
    val label: String,
    val icon: ImageVector,
    val navigate: (root: NavHostController, food: NavHostController?) -> Unit,
) {
    ADD_MEAL(
        label    = "Add new meal",
        icon     = Icons.Default.Restaurant,
        navigate = { _, food -> food?.navigate(Route.MealConfig) }
    )
}
