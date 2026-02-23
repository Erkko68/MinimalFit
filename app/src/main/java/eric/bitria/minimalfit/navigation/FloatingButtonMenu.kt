package eric.bitria.minimalfit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import eric.bitria.minimalfit.navigation.components.QuickActionItems

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FloatingActionMenu(
    navController: NavController
) {
    var expanded by remember { mutableStateOf(false) }
    val items = listOf(
        QuickActionItems.AddWorkout,
        QuickActionItems.AddMeal,
        QuickActionItems.LogWeight,
    )

    FloatingActionButtonMenu(
        expanded = expanded,
        button = {
            FloatingActionButton(
                onClick = { expanded = !expanded }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Quick Actions")
            }
        }
    ) {
        items.forEach { item ->
            FloatingActionButtonMenuItem(
                icon = {
                    Icon(item.icon, contentDescription = item.label)
                },
                text = {
                    Text(item.label)
                },
                onClick = {
                    expanded = false
                    navController.navigate(item.route)
                }
            )
        }
    }
}
