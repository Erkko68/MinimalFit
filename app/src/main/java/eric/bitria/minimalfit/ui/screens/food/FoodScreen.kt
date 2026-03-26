package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.data.entity.food.Diet
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.components.food.cards.MealCard
import eric.bitria.minimalfit.ui.components.food.lists.DietList
import eric.bitria.minimalfit.ui.components.shared.progress.DailyProgressPager
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.food.FoodViewModel
import eric.bitria.minimalfit.util.last7DaysEndingToday
import kotlinx.datetime.LocalDate
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodScreen(
    onNavigateToDailyLog: (LocalDate) -> Unit,
    onNavigateToDietDetail: (Diet) -> Unit,
    onNavigateToMealDetail: (Meal) -> Unit,
    viewModel: FoodViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val weekDates = remember { last7DaysEndingToday() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    ScreenConfiguration(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Food Tracker",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                scrollBehavior = scrollBehavior
            )
        },
        quickActions = true,
        bottomBar = true
    )

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val headerHeight = maxHeight * 0.5f

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalItemSpacing = Spacing.m,
            contentPadding = PaddingValues(Spacing.m)
        ) {
            // 1. HEADER SECTION
            item(span = StaggeredGridItemSpan.FullLine) {
                Column(modifier = Modifier.height(headerHeight)) {
                    DailyProgressPager(
                        uiState = uiState,
                        dates = weekDates,
                        onDayClick = onNavigateToDailyLog,
                        modifier = Modifier.weight(0.6f)
                    )

                    Text(
                        text = "Your Diets",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(vertical = Spacing.s)
                    )

                    DietList(
                        diets = uiState.diets,
                        onDietClick = onNavigateToDietDetail,
                        modifier = Modifier.weight(0.4f)
                    )
                }
            }

            // 2. MEALS TITLE
            item(span = StaggeredGridItemSpan.FullLine) {
                Text(
                    text = "Your Meals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            // 3. GRID ITEMS
            items(items = uiState.meals, key = { it.id }) { meal ->
                MealCard(
                    meal = meal,
                    onClick = { onNavigateToMealDetail(meal) }
                )
            }
        }
    }
}
