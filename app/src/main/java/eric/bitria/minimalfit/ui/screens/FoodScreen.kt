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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val dates by viewModel.mockDates.collectAsState()
    val filteredMeals by viewModel.filteredMeals.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()
    val selectedTag by viewModel.tagFilter.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = if (dates.isNotEmpty()) dates.size - 1 else 0,
        pageCount = { dates.size }
    )

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
                    meals = filteredMeals,
                    dates = dates,
                    pagerState = pagerState,
                    availableTags = availableTags,
                    selectedTag = selectedTag,
                    onTagSelected = { viewModel.setTagFilter(it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.FoodScreenContent(
    onRegisterClick: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    meals: List<SavedMeal>,
    dates: List<String>,
    pagerState: PagerState,
    availableTags: List<String>,
    selectedTag: String?,
    onTagSelected: (String?) -> Unit
) {
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
                    .fillMaxHeight(0.35f)
            ) { page ->
                RegisterMealCard(
                    date = dates[page],
                    onClick = { onRegisterClick(dates[page]) },
                    animatedVisibilityScope = animatedVisibilityScope,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Filter Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Your Saved Meals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = width * 0.05f)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = width * 0.05f),
                    horizontalArrangement = Arrangement.spacedBy(width * 0.02f),
                ) {
                    item {
                        FilterChip(
                            selected = selectedTag == null,
                            onClick = { onTagSelected(null) },
                            label = { Text("All") },
                            shape = RoundedCornerShape(percent = 50)
                        )
                    }
                    items(availableTags) { tag ->
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = { onTagSelected(tag) },
                            label = { Text(tag) },
                            shape = RoundedCornerShape(percent = 50)
                        )
                    }
                }
            }

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


