package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.data.entity.food.Diet
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.components.food.actions.PrimaryFloatingActionButton
import eric.bitria.minimalfit.ui.components.food.cards.DietCard
import eric.bitria.minimalfit.ui.components.food.cards.MealCard
import eric.bitria.minimalfit.ui.components.shared.progress.CalorieCircularProgressIndicator
import eric.bitria.minimalfit.ui.components.shared.progress.CarouselPager
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.food.FoodNavigationEvent
import eric.bitria.minimalfit.ui.viewmodels.food.FoodViewModel
import kotlinx.datetime.LocalDate
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodScreen(
    onNavigateToDailyLog: (LocalDate) -> Unit,
    onRegisterDailyMeal: () -> Unit,
    onNavigateToDietDetail: (Diet) -> Unit,
    onNavigateToMealDetail: (Meal) -> Unit,
    viewModel: FoodViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val dietCardSize = screenHeight * 0.13f

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is FoodNavigationEvent.ToDiet -> onNavigateToDietDetail(event.diet)
                is FoodNavigationEvent.ToMeal -> onNavigateToMealDetail(event.meal)
            }
        }
    }

    ScreenConfiguration(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Food Tracker",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            PrimaryFloatingActionButton(
                onClick = onRegisterDailyMeal,
                text = "Register new meal"
            )
        },
        quickActions = false,
        bottomBar = true
    )

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        verticalItemSpacing = Spacing.m,
        contentPadding = PaddingValues(
            bottom = Spacing.m,
            start = Spacing.m,
            end = Spacing.m
        )
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            CarouselPager(
                pageCount = uiState.weeklyProgress.size,
                onPageClick = { pageIndex ->
                    onNavigateToDailyLog(uiState.weeklyProgress[pageIndex].date)
                }
            ) { pageIndex ->
                val dailyData = uiState.weeklyProgress[pageIndex]
                val progress = if (dailyData.goalCalories > 0) {
                    dailyData.currentCalories.toFloat() / dailyData.goalCalories
                } else 0f

                CalorieCircularProgressIndicator(
                    progress = progress,
                    dayLabel = dailyData.dayLabel,
                    dayNumber = dailyData.dayNumber,
                    formattedCalories = NumberFormat.getIntegerInstance()
                        .format(dailyData.currentCalories),
                )
            }
        }

        item(span = StaggeredGridItemSpan.FullLine) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Diets",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
                IconButton(onClick = { viewModel.createDiet("New Diet") }) {
                    Icon(Icons.Filled.Add, contentDescription = "Create diet")
                }
            }
        }

        if (uiState.diets.isEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "No diets yet. Tap + to create one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(Spacing.m)
                    )
                }
            }
        } else {
            item(span = StaggeredGridItemSpan.FullLine) {
                val pagerState = rememberPagerState(pageCount = { uiState.diets.size })

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                    pageSize = PageSize.Fixed(dietCardSize),
                    pageSpacing = Spacing.m,
                    beyondViewportPageCount = 1
                ) { page ->
                    val diet = uiState.diets[page]
                    DietCard(
                        diet = diet,
                        onClick = { onNavigateToDietDetail(diet) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )
                }
            }
        }

        item(span = StaggeredGridItemSpan.FullLine) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Meals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
                IconButton(onClick = { viewModel.createMeal("New Meal") }) {
                    Icon(Icons.Filled.Add, contentDescription = "Create meal")
                }
            }
        }

        if (uiState.meals.isEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "No meals yet. Tap + to create one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(Spacing.m)
                    )
                }
            }
        } else {
            items(items = uiState.meals, key = { it.id }) { meal ->
                MealCard(
                    meal = meal,
                    onClick = { onNavigateToMealDetail(meal) }
                )
            }
        }
    }
}
