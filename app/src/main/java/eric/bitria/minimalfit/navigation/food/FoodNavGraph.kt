package eric.bitria.minimalfit.navigation.food

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import eric.bitria.minimalfit.navigation.Route
import eric.bitria.minimalfit.ui.components.food.MealSelectionScreen
import eric.bitria.minimalfit.ui.screens.FoodScreen
import eric.bitria.minimalfit.ui.screens.FoodSearchScreen
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FoodNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    SharedTransitionLayout(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = Route.Food
        ) {
            composable<Route.Food>(
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) },
                popEnterTransition = { fadeIn(tween(300)) },
                popExitTransition = { fadeOut(tween(300)) }
            ) {
                FoodScreen(
                    animatedVisibilityScope = this@composable,
                    onRegisterClick = { date: String -> navController.navigate(Route.MealSelection(date)) },
                    onSearchClick = { navController.navigate(Route.FoodSearch) }
                )
            }

            composable<Route.MealSelection>(
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) },
                popEnterTransition = { fadeIn(tween(300)) },
                popExitTransition = { fadeOut(tween(300)) }
            ) { backStackEntry ->
                val route = backStackEntry.toRoute<Route.MealSelection>()
                MealSelectionScreen(
                    date = route.date,
                    onBack = { navController.popBackStack() },
                    animatedVisibilityScope = this@composable,
                    viewModel = koinViewModel()
                )
            }

            composable<Route.FoodSearch>(
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) },
                popEnterTransition = { fadeIn(tween(300)) },
                popExitTransition = { fadeOut(tween(300)) }
            ) {
                FoodSearchScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
