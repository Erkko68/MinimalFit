package eric.bitria.minimalfit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FloatingActionMenu(navController: NavController) {
    var expanded by remember { mutableStateOf(false) }

    FloatingActionButtonMenu(
        expanded = expanded,
        button = {
            FloatingActionButton(onClick = { expanded = !expanded }) {
                Icon(Icons.Default.Add, contentDescription = "Quick Actions")
            }
        }
    ) {
        QuickAction.entries.forEach { action ->
            FloatingActionButtonMenuItem(
                icon = {
                    Icon(action.icon, contentDescription = action.label)
                },
                text = {
                    Text(action.label)
                },
                onClick = {
                    expanded = false
                    navController.navigate(action.route)
                }
            )
        }
    }
}
