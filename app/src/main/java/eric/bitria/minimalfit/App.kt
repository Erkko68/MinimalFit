package eric.bitria.minimalfit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import eric.bitria.minimalfit.ui.screens.track.TrackScreen
import eric.bitria.minimalfit.ui.screens.track.TrackDetailScreen
import eric.bitria.minimalfit.ui.screens.ProfileScreen
import eric.bitria.minimalfit.ui.screens.SettingsScreen
import eric.bitria.minimalfit.ui.screens.food.DailyLogScreen
import eric.bitria.minimalfit.ui.screens.food.FoodScreen
import eric.bitria.minimalfit.ui.screens.track.TrackRecordingScreen
import eric.bitria.minimalfit.ui.theme.Spacing
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
            QuickAction.REGISTER_WORKOUT -> {
                navController.navigate(Route.IndoorActivities)
            }
            QuickAction.START_TRACK -> {
                navController.navigate(Route.TrackRecording)
            }
            QuickAction.START_TIMER -> {

            }
        }
    }

    val showFab = NavDestination.entries.any { destination ->
        currentDestination?.hasRoute(destination.route::class) == true
    }
    val showTrackFab = currentDestination?.hasRoute(Route.OutdoorActivities::class) == true

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            Row {
                if (showTrackFab) {
                    ExtendedFloatingActionButton(
                        onClick = { navController.navigate(Route.TrackRecording) },
                        icon = { Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = "Start new track") },
                        text = { Text(text = "Start new track") }
                    )
                    Spacer(modifier = Modifier.width(Spacing.m))
                }
                if (showFab) {
                    QuickActionButton(
                        onActionClick = { action -> handleQuickAction(action) }
                    )
                }
            }
        }
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Food,
            modifier = Modifier.fillMaxSize()
        ) {
            composable<Route.Profile> {
                Box(Modifier.padding(contentPadding)) {
                    ProfileScreen()
                }
            }
            composable<Route.Settings> {
                Box(Modifier.padding(contentPadding)) {
                    SettingsScreen()
                }
            }
            composable<Route.Food> {
                Box(Modifier.padding(contentPadding)) {
                    FoodScreen(
                        onNavigateToDailyLog = { date ->
                            navController.navigate(Route.DailyLog(date = date.toString()))
                        }
                    )
                }
            }
            composable<Route.OutdoorActivities> {
                Box(Modifier.padding(contentPadding)) {
                    TrackScreen(
                        onTrackClick = { trackId ->
                            navController.navigate(Route.TrackDetail(trackId = trackId))
                        }
                    )
                }
            }
            composable<Route.TrackRecording> {
                Box(Modifier.padding(bottom = contentPadding.calculateBottomPadding())) {
                    TrackRecordingScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
            composable<Route.IndoorActivities> {
                Box(Modifier.padding(contentPadding)) {
                    IndoorActivitiesScreen()
                }
            }
            composable<Route.TrackDetail> { backStackEntry ->
                Box(Modifier.padding(bottom = contentPadding.calculateBottomPadding())) {
                    val trackDetail = backStackEntry.toRoute<Route.TrackDetail>()
                    TrackDetailScreen(
                        trackId = trackDetail.trackId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
            composable<Route.DailyLog> { backStackEntry ->
                Box(Modifier.padding(contentPadding)) {
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
}