package eric.bitria.minimalfit

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import eric.bitria.minimalfit.navigation.AppNavHost
import eric.bitria.minimalfit.navigation.NavDestination
import eric.bitria.minimalfit.navigation.QuickAction
import eric.bitria.minimalfit.navigation.composables.BottomNavigationBar
import eric.bitria.minimalfit.navigation.composables.QuickActionButton

// TODO FER QUE LA APP REACCIONI AL ESTAT DE LA XARXA, DATOS ACTIVATS O WIFI NO ACTIVATS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentTopLevelDestination = NavDestination.fromNavDestination(currentDestination)

    val showBottomBar = currentTopLevelDestination?.showBottomBar == true
    val showGlobalQuickActions = currentTopLevelDestination != null

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        },
        floatingActionButton = {
            if (showGlobalQuickActions) {
                QuickActionButton(
                    actions = QuickAction.entries,
                    onActionClick = { action ->
                        action.createRoute()?.let { navController.navigate(it) }
                    }
                )
            }
        }
    ) { contentPadding ->
        AppNavHost(
            navController = navController,
            contentPadding = contentPadding
        )
    }
}