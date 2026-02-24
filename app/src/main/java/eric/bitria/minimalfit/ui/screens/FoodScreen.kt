package eric.bitria.minimalfit.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.ui.components.food.MealSelectionScreen
import eric.bitria.minimalfit.ui.components.food.RegisterMealCard
import eric.bitria.minimalfit.ui.components.food.SavedMealCard
import eric.bitria.minimalfit.ui.viewmodels.FoodViewModel
import eric.bitria.minimalfit.ui.viewmodels.SavedMeal
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FoodScreen(viewModel: FoodViewModel = koinViewModel()) {
    var activeRegistrationDate by remember { mutableStateOf<String?>(null) }
    val savedMeals by viewModel.savedMeals.collectAsState()
    val dates by viewModel.mockDates.collectAsState()

    SharedTransitionLayout {
        AnimatedContent(
            targetState = activeRegistrationDate,
            label = "container_transform_transition",
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            },
        ) { targetDate ->
            if (targetDate != null) {
                MealSelectionScreen(
                    date = targetDate,
                    onBack = { activeRegistrationDate = null },
                    animatedVisibilityScope = this@AnimatedContent,
                    viewModel = viewModel
                )
            } else {
                FoodScreenContent(
                    onRegisterClick = { clickedDate -> activeRegistrationDate = clickedDate },
                    animatedVisibilityScope = this@AnimatedContent,
                    meals = savedMeals,
                    dates = dates
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.FoodScreenContent(
    onRegisterClick: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    meals: List<SavedMeal>,
    dates: List<String>
) {
    val pagerState = rememberPagerState(initialPage = 2, pageCount = { dates.size })

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = maxWidth
        val height = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = height * 0.02f)
        ) {
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = width * 0.1f),
                pageSpacing = width * 0.04f,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f) // Reserve 35% of height for the pager
            ) { page ->
                RegisterMealCard(
                    date = dates[page],
                    onClick = { onRegisterClick(dates[page]) },
                    animatedVisibilityScope = animatedVisibilityScope,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(height * 0.04f))

            Text(
                text = "Your Saved Meals",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = width * 0.05f)
                    .padding(bottom = height * 0.01f)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(height * 0.015f),
                contentPadding = PaddingValues(
                    horizontal = width * 0.05f, 
                    vertical = height * 0.01f
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                items(meals) { meal ->
                    SavedMealCard(meal)
                }
            }
        }
    }
}
