package eric.bitria.minimalfit.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import eric.bitria.minimalfit.ui.screens.food.DailyLogScreen
import eric.bitria.minimalfit.ui.screens.food.DietDetailScreen
import eric.bitria.minimalfit.ui.screens.food.FoodScreen
import eric.bitria.minimalfit.ui.screens.food.MealDetailScreen
import eric.bitria.minimalfit.ui.screens.gym.GymScreen
import eric.bitria.minimalfit.ui.screens.gym.GymSessionScreen
import eric.bitria.minimalfit.ui.screens.gym.ExerciseProgressionScreen
import eric.bitria.minimalfit.ui.screens.profile.ProfileScreen
import eric.bitria.minimalfit.ui.screens.profile.SettingsScreen
import eric.bitria.minimalfit.ui.screens.track.TrackDetailScreen
import eric.bitria.minimalfit.ui.screens.track.TrackRecordingScreen
import eric.bitria.minimalfit.ui.screens.track.TrackScreen
import kotlinx.datetime.LocalDate

@Composable
fun AppNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Route.Profile,
        modifier = Modifier.fillMaxSize()
    ) {
        composable<Route.Profile> {
            ProfileScreen(
                onSettingsClick = {
                    navController.navigate(Route.Settings)
                }
            )
        }
        composable<Route.Settings> {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable<Route.Food> {
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
        composable<Route.DailyLog> { backStackEntry ->
            val args = backStackEntry.toRoute<Route.DailyLog>()
            val date = LocalDate.parse(args.date)
            DailyLogScreen(
                date = date,
                openSearch = args.openSearch,
                onBackClick = { navController.popBackStack() },
                onNavigateToMealDetail = { meal ->
                    navController.navigate(Route.MealDetail(mealId = meal.id))
                }
            )
        }
        composable<Route.DietDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<Route.DietDetail>()
            DietDetailScreen(
                dietId = args.dietId,
                onBackClick = { navController.popBackStack() },
                onNavigateToMealDetail = { meal ->
                    navController.navigate(Route.MealDetail(mealId = meal.id))
                }
            )
        }
        composable<Route.MealDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<Route.MealDetail>()
            MealDetailScreen(
                mealId = args.mealId,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable<Route.OutdoorActivities> {
            TrackScreen(
                onTrackClick = { trackId ->
                    navController.navigate(Route.TrackDetail(trackId = trackId))
                },
                onNewTrackClick = {
                    navController.navigate(Route.TrackRecording)
                }
            )
        }
        composable<Route.TrackRecording> {
            TrackRecordingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<Route.IndoorActivities> {
            GymScreen(
                onNavigateToSession = { sessionId ->
                    navController.navigate(Route.GymSession(sessionId = sessionId))
                },
                onNavigateToExerciseProgression = { exerciseId ->
                    navController.navigate(Route.ExerciseProgression(exerciseId = exerciseId))
                }
            )
        }
        composable<Route.GymSession> { backStackEntry ->
            val args = backStackEntry.toRoute<Route.GymSession>()
            GymSessionScreen(
                sessionId = args.sessionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<Route.TrackDetail> { backStackEntry ->
            val trackDetail = backStackEntry.toRoute<Route.TrackDetail>()
            TrackDetailScreen(
                trackId = trackDetail.trackId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<Route.ExerciseProgression> { backStackEntry ->
            val args = backStackEntry.toRoute<Route.ExerciseProgression>()
            ExerciseProgressionScreen(
                exerciseId = args.exerciseId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
