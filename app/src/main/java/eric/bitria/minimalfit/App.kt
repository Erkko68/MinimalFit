package eric.bitria.minimalfit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import eric.bitria.minimalfit.navigation.NavDestination
import eric.bitria.minimalfit.navigation.QuickAction
import eric.bitria.minimalfit.navigation.Route
import eric.bitria.minimalfit.navigation.composables.BottomNavigationBar
import eric.bitria.minimalfit.navigation.composables.QuickActionButton
import eric.bitria.minimalfit.ui.screens.IndoorActivitiesScreen
import eric.bitria.minimalfit.ui.screens.OutdoorActivitiesScreen
import eric.bitria.minimalfit.ui.screens.ProfileScreen
import eric.bitria.minimalfit.ui.screens.SettingsScreen
import eric.bitria.minimalfit.ui.screens.food.DailyLogScreen
import eric.bitria.minimalfit.ui.screens.food.FoodScreen
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    fun handleQuickAction(action: QuickAction) {
        when (action) {
            QuickAction.ADD_MEAL -> {
                navController.navigate(Route.DailyLog(date = LocalDate.now().toString(), openSearch = true))
            }
            QuickAction.START_WORKOUT -> {
                navController.navigate(Route.IndoorActivities)
            }
        }
    }

    val showFab = NavDestination.entries.any { destination ->
        currentDestination?.hasRoute(destination.route::class) == true
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            if (showFab) {
                QuickActionButton(
                    onActionClick = { action -> handleQuickAction(action) }
                )
            }
        }
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Food,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            composable<Route.Profile> { ProfileScreen() }
            composable<Route.Settings> { SettingsScreen() }
            composable<Route.Food> {
                FoodScreen(
                    onNavigateToDailyLog = { date ->
                        navController.navigate(Route.DailyLog(date = date.toString()))
                    }
                )
            }
            composable<Route.OutdoorActivities> { OutdoorActivitiesScreen() }
            composable<Route.IndoorActivities> { IndoorActivitiesScreen() }
            composable<Route.DailyLog> { backStackEntry ->
                val dailyLog = backStackEntry.toRoute<Route.DailyLog>()
                DailyLogScreen(
                    date = LocalDate.parse(dailyLog.date),
                    openSearch = dailyLog.openSearch,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}