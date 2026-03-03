package eric.bitria.minimalfit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eric.bitria.minimalfit.navigation.BottomNavigationBar
import eric.bitria.minimalfit.navigation.QuickActionFab
import eric.bitria.minimalfit.navigation.Route
import eric.bitria.minimalfit.navigation.food.FoodNavHost
import eric.bitria.minimalfit.ui.screens.IndoorActivitiesScreen
import eric.bitria.minimalfit.ui.screens.OutdoorActivitiesScreen
import eric.bitria.minimalfit.ui.screens.ProfileScreen
import eric.bitria.minimalfit.ui.screens.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val navController = rememberNavController()

    // Inner food nav controller — captured when the food graph is composed
    var foodNavController by remember { mutableStateOf<NavHostController?>(null) }

    // FAB expansion state
    var fabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            QuickActionFab(
                expanded = fabExpanded,
                onToggle = { fabExpanded = !fabExpanded },
                rootNavController = navController,
                foodNavController = foodNavController,
                onActionDispatched = { fabExpanded = false }
            )
        }
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Profile,
            modifier = Modifier
                .padding(bottom = contentPadding.calculateBottomPadding())
        ) {
            composable<Route.Profile> { ProfileScreen() }
            composable<Route.Settings> { SettingsScreen() }
            composable<Route.Food> {
                FoodNavHost(
                    modifier = Modifier.fillMaxSize(),
                    controllerRef = { foodNavController = it }
                )
            }
            composable<Route.OutdoorActivities> { OutdoorActivitiesScreen() }
            composable<Route.IndoorActivities> { IndoorActivitiesScreen() }
        }
    }
}
