package eric.bitria.minimalfit

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
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
                        action.createRoute()?.let { route ->
                            navController.navigate(route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
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