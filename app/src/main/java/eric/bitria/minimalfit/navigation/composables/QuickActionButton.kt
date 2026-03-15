package eric.bitria.minimalfit.navigation.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import eric.bitria.minimalfit.navigation.QuickAction

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuickActionButton(
    actions: List<QuickAction>,
    onActionClick: (QuickAction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (actions.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }

    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = { expanded = it }
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (expanded) "Close actions" else "Quick actions"
                )
            }
        }
    ) {
        actions.forEach { action ->
            FloatingActionButtonMenuItem(
                onClick = {
                    onActionClick(action)
                    expanded = false
                },
                icon = {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = null
                    )
                },
                text = { Text(text = action.label) }
            )
        }
    }
}