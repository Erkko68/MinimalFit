package eric.bitria.minimalfit

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eric.bitria.minimalfit.navigation.BottomNavigationBar
import eric.bitria.minimalfit.navigation.FloatingActionMenu
import eric.bitria.minimalfit.navigation.Route
import eric.bitria.minimalfit.ui.screens.FoodScreen
import eric.bitria.minimalfit.ui.screens.ProfileScreen
import eric.bitria.minimalfit.ui.screens.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = { FloatingActionMenu(navController) }
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Profile,
            modifier = Modifier.padding(contentPadding)
        ) {
            composable<Route.Profile> { ProfileScreen() }
            composable<Route.Settings> { SettingsScreen() }
            composable<Route.Food> { FoodScreen() }
            composable<Route.OutdoorActivities> { FoodScreen() }
            composable<Route.IndoorActivities> { FoodScreen() }
        }
    }
}
