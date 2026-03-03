package eric.bitria.minimalfit.navigation.food

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import eric.bitria.minimalfit.navigation.Route
import eric.bitria.minimalfit.ui.screens.food.FoodScreen
import eric.bitria.minimalfit.ui.screens.food.FoodSearchScreen
import eric.bitria.minimalfit.ui.screens.food.MealConfigScreen
import eric.bitria.minimalfit.ui.screens.food.MealSelectionScreen
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FoodNavHost(
    modifier: Modifier = Modifier,
    controllerRef: ((NavHostController) -> Unit)? = null
) {
    val navController = rememberNavController()

    // Expose the inner nav controller so the parent (App) can trigger navigation
    controllerRef?.invoke(navController)

    SharedTransitionLayout(modifier = modifier) {
        NavHost(navController = navController, startDestination = Route.Food) {

            composable<Route.Food>(
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) },
                popEnterTransition = { fadeIn(tween(300)) },
                popExitTransition = { fadeOut(tween(300)) }
            ) {
                FoodScreen(
                    animatedVisibilityScope = this@composable,
                    onRegisterClick = { date: LocalDate ->
                        navController.navigate(Route.MealSelection(date.toString()))
                    },
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
                val date = LocalDate.parse(route.date)
                MealSelectionScreen(
                    date = date,
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
                FoodSearchScreen(onBack = { navController.popBackStack() })
            }

            composable<Route.MealConfig>(
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) },
                popEnterTransition = { fadeIn(tween(300)) },
                popExitTransition = { fadeOut(tween(300)) }
            ) {
                MealConfigScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
