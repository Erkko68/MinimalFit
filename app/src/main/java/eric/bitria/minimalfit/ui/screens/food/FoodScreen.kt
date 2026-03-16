package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.data.model.food.Diet
import eric.bitria.minimalfit.data.model.food.Meal
import eric.bitria.minimalfit.ui.components.animations.StaggeredSnapLayoutInfoProvider
import eric.bitria.minimalfit.ui.components.food.cards.MealCard
import eric.bitria.minimalfit.ui.components.food.lists.DietList
import eric.bitria.minimalfit.ui.components.shared.progress.DailyProgressPager
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.util.WeekViewHelper
import eric.bitria.minimalfit.ui.viewmodels.food.FoodViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.time.LocalDate

@Composable
fun FoodScreen(
    onNavigateToDailyLog: (LocalDate) -> Unit,
    onNavigateToDietDetail: (Diet) -> Unit,
    onNavigateToMealDetail: (Meal) -> Unit,
    viewModel: FoodViewModel = koinViewModel(),
    weekViewHelper: WeekViewHelper = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val weekDates = remember { weekViewHelper.last7Days() }

    val state = rememberLazyStaggeredGridState()
    val snappingLayout = remember(state) { StaggeredSnapLayoutInfoProvider(state) }
    val flingBehavior = rememberSnapFlingBehavior(snappingLayout)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val headerHeight = maxHeight * 0.5f

        LazyVerticalStaggeredGrid(
            state = state,
            columns = StaggeredGridCells.Fixed(2),
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalItemSpacing = Spacing.m,
            contentPadding = PaddingValues(
                bottom = Spacing.m,
                start = Spacing.m,
                end = Spacing.m
            )
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
