package eric.bitria.minimalfit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
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
import eric.bitria.minimalfit.ui.viewmodels.DailyLogViewModel
import eric.bitria.minimalfit.ui.viewmodels.FoodViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val navController = rememberNavController()
    val dailyLogViewModel: DailyLogViewModel = koinViewModel()
    val foodViewModel: FoodViewModel = koinViewModel()

    val backStackEntry by navController.currentBackStackEntryAsState()

    fun handleQuickAction(action: QuickAction) {
        when (action) {
            QuickAction.ADD_MEAL -> {
                val currentRoute = backStackEntry?.destination?.route ?: ""
                if (currentRoute.contains("DailyLog")) {
                    dailyLogViewModel.openSearchDialog()
                } else {
                    navController.navigate(Route.DailyLog(dailyLogViewModel.todayIndex()))
                    dailyLogViewModel.openSearchDialog()
                }
            }
            QuickAction.START_WORKOUT -> {
                navController.navigate(Route.IndoorActivities)
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            QuickActionButton(
                onActionClick = { action -> handleQuickAction(action) }
            )
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
                    onNavigateToDailyLog = { dayIndex ->
                        navController.navigate(Route.DailyLog(dayIndex))
                    }
                )
            }
            composable<Route.OutdoorActivities> { OutdoorActivitiesScreen() }
            composable<Route.IndoorActivities> { IndoorActivitiesScreen() }
            composable<Route.DailyLog> { backStackEntry ->
                val dailyLog = backStackEntry.toRoute<Route.DailyLog>()
                DailyLogScreen(
                    dayIndex = dailyLog.dayIndex,
                    onBackClick = { navController.popBackStack() },
                    foodViewModel = foodViewModel,
                    dailyLogViewModel = dailyLogViewModel
                )
            }
        }
    }
}