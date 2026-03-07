package eric.bitria.minimalfit

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eric.bitria.minimalfit.navigation.Route
import eric.bitria.minimalfit.navigation.composables.BottomNavigationBar
import eric.bitria.minimalfit.ui.screens.IndoorActivitiesScreen
import eric.bitria.minimalfit.ui.screens.OutdoorActivitiesScreen
import eric.bitria.minimalfit.ui.screens.ProfileScreen
import eric.bitria.minimalfit.ui.screens.SettingsScreen
import eric.bitria.minimalfit.ui.screens.food.FoodScreen

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
        //floatingActionButton = {}
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Profile,
            modifier = Modifier
                .padding(bottom = contentPadding.calculateBottomPadding())
        ) {
            composable<Route.Profile> { ProfileScreen() }
            composable<Route.Settings> { SettingsScreen() }
            composable<Route.Food> { FoodScreen() }
            composable<Route.OutdoorActivities> { OutdoorActivitiesScreen() }
            composable<Route.IndoorActivities> { IndoorActivitiesScreen() }
        }
    }
}
