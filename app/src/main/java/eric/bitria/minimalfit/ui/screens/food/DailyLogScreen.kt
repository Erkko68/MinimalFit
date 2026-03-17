package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.ui.components.animations.SwipeToDeleteCard
import eric.bitria.minimalfit.ui.components.food.actions.AddEntryFab
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

@OptIn(ExperimentalFoundationApi::class)
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

    val state = rememberLazyStaggeredGridState()
    val backgroundColor = MaterialTheme.colorScheme.background

    Scaffold(
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            AddEntryFab(
                onClick = { dailyLogViewModel.openSearchDialog() },
                text = "Add Meal"
            )
        }
    ) { paddingValues ->

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
                .background(backgroundColor)
        ) {
            val headerHeight = maxHeight * 0.35f // Aligned to DietDetailScreen proportions
            var headerAreaHeight by remember { mutableIntStateOf(0) }

            // 1. SCROLLABLE CONTENT (Rendered first to be below the header)
            LazyVerticalStaggeredGrid(
                state = state,
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(
                    start = Spacing.m,
                    end = Spacing.m,
                    top = with(LocalDensity.current) { headerAreaHeight.toDp() },
                    bottom = Spacing.m
                ),
                horizontalArrangement = Arrangement.spacedBy(Spacing.m),
                verticalItemSpacing = Spacing.m
            ) {

                if (uiState.logs.isEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        EmptyMealsPlaceholder()
                    }
                } else {
                    items(
                        items = uiState.logs,
                        key = { it.logId }
                    ) { model ->
                        SwipeToDeleteCard(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.extraLarge)
                                .animateItem(
                                    fadeInSpec = tween(300),
                                    fadeOutSpec = tween(300),
                                    placementSpec = tween(300)
                                ),
                            onDismiss = {
                                dailyLogViewModel.removeMealLog(model.logId)
                            }
                        ) {
                            MealCard(
                                meal = model.meal,
                                calories = model.calories,
                                onClick = { onNavigateToMealDetail(model.meal) }
                            )
                        }
                    }
                }
            }

            // 2. FIXED HEADER SECTION (Rendered second to be on top)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { headerAreaHeight = it.height }
                    .background(backgroundColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(headerHeight)
                ) {
                    // Daily Calorie Circle Card as the "Hero" element
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

                    // Added gradient to mirror DietDetailScreen layout
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0.3f to Color.Transparent,
                                    1.0f to backgroundColor
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .padding(horizontal = Spacing.m, vertical = Spacing.s)
                    ) {
                        Text(
                            text = "Daily Log",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}, ${date.day} ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(Spacing.m))
                        Text(
                            text = "Meals",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 3. FIXED BACK BUTTON
            FilledIconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(Spacing.m),
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