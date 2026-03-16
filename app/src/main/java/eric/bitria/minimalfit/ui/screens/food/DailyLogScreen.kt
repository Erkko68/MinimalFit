package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import eric.bitria.minimalfit.data.repository.food.FoodCatalogRepository
import eric.bitria.minimalfit.ui.components.animations.StaggeredSnapLayoutInfoProvider
import eric.bitria.minimalfit.ui.components.animations.SwipeToDeleteCard
import eric.bitria.minimalfit.ui.components.food.actions.AddEntryFab
import eric.bitria.minimalfit.ui.components.food.cards.DailyCalorieCircleCard
import eric.bitria.minimalfit.ui.components.food.cards.MealCard
import eric.bitria.minimalfit.ui.components.food.dialogs.MealSearchDialog
import eric.bitria.minimalfit.ui.components.food.lists.EmptyMealsPlaceholder
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.food.DailyCalorieData
import eric.bitria.minimalfit.ui.viewmodels.food.DailyLogViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import java.time.LocalDate

@Composable
fun DailyLogScreen(
    date: LocalDate,
    openSearch: Boolean = false,
    onBackClick: () -> Unit,
    dailyLogViewModel: DailyLogViewModel = koinViewModel { parametersOf(date) },
    foodCatalog: FoodCatalogRepository = koinInject()
) {
    val uiState by dailyLogViewModel.uiState.collectAsState()
    val savedMeals by foodCatalog.getMeals().collectAsState(initial = emptyList())

    LaunchedEffect(openSearch) {
        if (openSearch) {
            dailyLogViewModel.openSearchDialog()
        }
    }

    val progress = if (uiState.calorieGoal > 0) {
        uiState.meals.sumOf { it.meal.calories }.toFloat() / uiState.calorieGoal
    } else 0f

    val dailyData = DailyCalorieData(
        dayLabel = date.dayOfWeek.name.take(3),
        dayNumber = date.dayOfMonth,
        currentCalories = uiState.meals.sumOf { it.meal.calories },
        goalCalories = uiState.calorieGoal
    )

    // --- Snapping Logic Setup ---
    val state = rememberLazyStaggeredGridState()
    val snappingLayout = remember(state) { StaggeredSnapLayoutInfoProvider(state) }
    val flingBehavior = rememberSnapFlingBehavior(snappingLayout)

    Scaffold(
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            AddEntryFab(onClick = { dailyLogViewModel.openSearchDialog() })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            LazyVerticalStaggeredGrid(
                state = state,
                columns = StaggeredGridCells.Fixed(2),
                flingBehavior = flingBehavior,
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.m),
                verticalItemSpacing = Spacing.m,
                contentPadding = PaddingValues(Spacing.m)
            ) {
                // 1. HEADER SECTION: Back Button and Circle Progress
                item(span = StaggeredGridItemSpan.FullLine) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate back"
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .aspectRatio(1f)
                                .align(Alignment.TopCenter)
                        ) {
                            DailyCalorieCircleCard(dailyData, progress)
                        }
                    }
                }

                // 2. MEALS TITLE
                item(span = StaggeredGridItemSpan.FullLine) {
                    Text(
                        text = "Meals",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.02).em,
                    )
                }

                // 3. MEALS LIST OR PLACEHOLDER
                if (uiState.meals.isEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        EmptyMealsPlaceholder()
                    }
                } else {
                    items(
                        items = uiState.meals,
                        key = { it.id }
                    ) { mealLog ->
                        SwipeToDeleteCard(
                            onDismiss = { dailyLogViewModel.removeMeal(mealLog) }
                        ) {
                            MealCard(meal = mealLog.meal)
                        }
                    }
                }
            }
        }
    }

    if (uiState.showSearchDialog) {
        MealSearchDialog(
            savedMeals = savedMeals,
            onDismiss = { dailyLogViewModel.dismissSearchDialog() },
            onAddMeal = { meal -> dailyLogViewModel.addMeal(meal) }
        )
    }
}