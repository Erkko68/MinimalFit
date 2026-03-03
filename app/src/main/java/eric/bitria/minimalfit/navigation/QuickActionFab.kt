package eric.bitria.minimalfit.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

/**
 * Global quick-action FAB.
 *
 * Renders a circular toggle FAB. When expanded, it reveals one
 * [ExtendedFloatingActionButton] per [QuickAction] entry.
 * Navigation is fully handled by [QuickAction.navigate] — no routing
 * logic lives here.
 *
 * @param expanded          Whether the action list is open.
 * @param onToggle          Called when the main FAB is tapped.
 * @param rootNavController Root app nav controller.
 * @param foodNavController Inner food-graph nav controller (nullable until the food tab is visited).
 * @param onActionDispatched Called after any action is triggered (e.g. to collapse the FAB).
 * @param actions           Ordered list of actions to display (defaults to all).
 */
@Composable
fun QuickActionFab(
    expanded: Boolean,
    onToggle: () -> Unit,
    rootNavController: NavHostController,
    foodNavController: NavHostController?,
    onActionDispatched: () -> Unit = {},
    actions: List<QuickAction> = QuickAction.entries,
) {
    BoxWithConstraints {
        val fabSize   = maxWidth * 0.16f
        val iconSize  = fabSize * 0.44f
        val spacing   = maxWidth * 0.03f

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            // Action items — revealed when expanded
            actions.forEach { action ->
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(tween(150)) + scaleIn(tween(150)),
                    exit  = fadeOut(tween(150)) + scaleOut(tween(150))
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            onActionDispatched()
                            action.navigate(rootNavController, foodNavController)
                        },
                        icon = {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = null
                            )
                        },
                        text = { Text(action.label) },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor   = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Main toggle FAB
            FloatingActionButton(
                onClick = onToggle,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(fabSize)
            ) {
                AnimatedContent(targetState = expanded) { isExpanded ->
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (isExpanded) "Close" else "Quick actions",
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
    }
}
