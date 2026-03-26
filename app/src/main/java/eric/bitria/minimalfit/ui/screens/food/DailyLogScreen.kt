package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.components.animations.SwipeToDeleteCard
import eric.bitria.minimalfit.ui.components.food.FlexibleTopBar
import eric.bitria.minimalfit.ui.components.food.actions.PrimaryFloatingActionButton
import eric.bitria.minimalfit.ui.components.food.cards.DailyCalorieCircleCard
import eric.bitria.minimalfit.ui.components.food.cards.MealCard
import eric.bitria.minimalfit.ui.components.food.dialogs.SearchableItemDialog
import eric.bitria.minimalfit.ui.components.food.dialogs.item.MealItem
import eric.bitria.minimalfit.ui.components.food.lists.EmptyMealsPlaceholder
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.food.DailyCalorieData
import eric.bitria.minimalfit.ui.viewmodels.food.DailyLogViewModel
import kotlinx.datetime.LocalDate
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DailyLogScreen(
    date: LocalDate,
    openSearch: Boolean = false,
    onBackClick: () -> Unit,
    onNavigateToMealDetail: (Meal) -> Unit,
    dailyLogViewModel: DailyLogViewModel = koinViewModel { parametersOf(date) }
) {
    val uiState by dailyLogViewModel.uiState.collectAsState()

    LaunchedEffect(openSearch) {
        if (openSearch) {
            dailyLogViewModel.openSearchDialog()
        }
    }

    val progress = if (uiState.calorieGoal > 0) {
        uiState.totalCalories.toFloat() / uiState.calorieGoal
    } else 0f

    val dailyData = DailyCalorieData(
        dayLabel = date.dayOfWeek.name.take(3),
        dayNumber = date.day,
        currentCalories = uiState.totalCalories,
        goalCalories = uiState.calorieGoal
    )

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    ScreenConfiguration(
        topBar = {
            FlexibleTopBar(
                title = {
                    Text(
                        text = "Daily Log",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold)
                    )
                },
                subtitle = {
                    Text(
                        text = "${date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}, ${date.day} ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                navigationIcon = {
                    FilledIconButton(
                        onClick = onBackClick,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                backgroundImage = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(0.6f)
                                .aspectRatio(1f)
                        ) {
                            DailyCalorieCircleCard(dailyData, progress)
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            PrimaryFloatingActionButton(
                onClick = { dailyLogViewModel.openSearchDialog() },
                text = "Add Meal"
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = false,
        quickActions = false
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.m,
                end = Spacing.m,
                top = Spacing.m,
                bottom = Spacing.xl * 2
            ),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalItemSpacing = Spacing.m
        ) {
            if (uiState.logs.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    EmptyMealsPlaceholder()
                }
            }

            items(uiState.logs, key = { it.logId }) { log ->
                SwipeToDeleteCard(
                    onDismiss = { dailyLogViewModel.removeMealLog(log.logId) },
                    modifier = Modifier.animateItem()
                ) {
                    MealCard(
                        meal = log.meal,
                        calories = log.calories,
                        onClick = { onNavigateToMealDetail(log.meal) }
                    )
                }
            }
        }


        if (uiState.showSearchDialog) {
            SearchableItemDialog(
                title = "Log Meal",
                placeholder = "e.g., Chicken Salad",
                items = uiState.savedMeals,
                itemKey = { it.id },
                filter = { meal, query ->
                    meal.name.contains(query, ignoreCase = true)
                },
                onDismiss = { dailyLogViewModel.dismissSearchDialog() },
                itemContent = { meal ->
                    MealItem(
                        meal = meal,
                        onAdd = { amount, portionMode ->
                            dailyLogViewModel.addMeal(meal.id, amount, portionMode)
                            dailyLogViewModel.dismissSearchDialog()
                        }
                    )
                }
            )
        }
    }
}
