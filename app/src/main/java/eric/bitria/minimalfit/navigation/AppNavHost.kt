package eric.bitria.minimalfit.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import eric.bitria.minimalfit.ui.screens.IndoorActivitiesScreen
import eric.bitria.minimalfit.ui.screens.ProfileScreen
import eric.bitria.minimalfit.ui.screens.SettingsScreen
import eric.bitria.minimalfit.ui.screens.food.DailyLogScreen
import eric.bitria.minimalfit.ui.screens.food.DietDetailScreen
import eric.bitria.minimalfit.ui.screens.food.FoodScreen
import eric.bitria.minimalfit.ui.screens.food.MealDetailScreen
import eric.bitria.minimalfit.ui.screens.track.TrackDetailScreen
import eric.bitria.minimalfit.ui.screens.track.TrackRecordingScreen
import eric.bitria.minimalfit.ui.screens.track.TrackScreen
import kotlinx.datetime.LocalDate

@Composable
fun AppNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues,
) {
    NavHost(
        navController = navController,
        startDestination = Route.Profile,
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
                    },
                    onNavigateToDietDetail = { diet ->
                        navController.navigate(Route.DietDetail(dietId = diet.id))
                    },
                    onNavigateToMealDetail = { meal ->
                        navController.navigate(Route.MealDetail(mealId = meal.id))
                    }
                )
            }
        }
        composable<Route.OutdoorActivities> {
            Box(Modifier.padding(contentPadding)) {
                TrackScreen(
                    onTrackClick = { trackId ->
                        navController.navigate(Route.TrackDetail(trackId = trackId))
                    },
                    onNewTrackClick = {
                        navController.navigate(Route.TrackRecording)
                    }
                )
            }
        }
        composable<Route.TrackRecording> {
            TrackRecordingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<Route.IndoorActivities> {
            Box(Modifier.padding(contentPadding)) {
                IndoorActivitiesScreen()
            }
        }
        composable<Route.TrackDetail> { backStackEntry ->
            val trackDetail = backStackEntry.toRoute<Route.TrackDetail>()
            TrackDetailScreen(
                trackId = trackDetail.trackId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<Route.DailyLog> { backStackEntry ->
            val dailyLog = backStackEntry.toRoute<Route.DailyLog>()
            DailyLogScreen(
                date = LocalDate.parse(dailyLog.date),
                openSearch = dailyLog.openSearch,
                onBackClick = { navController.popBackStack() },
                onNavigateToMealDetail = { meal ->
                    navController.navigate(Route.MealDetail(mealId = meal.id))
                }
            )
        }
        composable<Route.DietDetail> { backStackEntry ->
            val dietDetail = backStackEntry.toRoute<Route.DietDetail>()
            DietDetailScreen(
                dietId = dietDetail.dietId,
                onBackClick = { navController.popBackStack() },
                onNavigateToMealDetail = { meal ->
                    navController.navigate(Route.MealDetail(mealId = meal.id))
                }
            )
        }
        composable<Route.MealDetail> { backStackEntry ->
            val mealDetail = backStackEntry.toRoute<Route.MealDetail>()
            MealDetailScreen(
                mealId = mealDetail.mealId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
