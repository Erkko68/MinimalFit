package eric.bitria.minimalfit

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eric.bitria.minimalfit.navigation.Screens
import eric.bitria.minimalfit.ui.screens.HomeScreen
import eric.bitria.minimalfit.ui.screens.SettingsScreen

@Composable
fun App() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screens.Home) {
        composable<Screens.Home> {
            HomeScreen()
        }
        composable<Screens.Settings> {
            SettingsScreen()
        }
    }
}