package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import eric.bitria.minimalfit.data.model.Diet
import eric.bitria.minimalfit.data.repository.food.FoodCatalogRepository
import eric.bitria.minimalfit.ui.components.food.cards.MealCard
import eric.bitria.minimalfit.ui.components.food.lists.DietList
import eric.bitria.minimalfit.ui.components.food.lists.StaggeredGrid
import eric.bitria.minimalfit.ui.components.food.progress.DailyProgressPager
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
    viewModel: FoodViewModel = koinViewModel(),
    weekViewHelper: WeekViewHelper = koinInject(),
    foodCatalog: FoodCatalogRepository = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val weekDates = weekViewHelper.last7Days()

    var isCollapsed by remember { mutableStateOf(false) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            // Track the continuous scroll distance
            var accumulatedScroll = 0f

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y

                // If the user changes scroll direction, reset the accumulator
                if ((delta < 0 && accumulatedScroll > 0) || (delta > 0 && accumulatedScroll < 0)) {
                    accumulatedScroll = 0f
                }

                accumulatedScroll += delta

                val collapseThreshold = -400f
                val expandThreshold = 400f

                if (accumulatedScroll < collapseThreshold && !isCollapsed) {
                    isCollapsed = true
                    accumulatedScroll = 0f // Reset after triggering
                } else if (accumulatedScroll > expandThreshold && isCollapsed) {
                    isCollapsed = false
                    accumulatedScroll = 0f // Reset after triggering
                }

                // Return Offset.Zero so the StaggeredGrid still gets to consume the scroll naturally
                return Offset.Zero
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.m)
            .nestedScroll(nestedScrollConnection)
    ) {
        val expandedTopHeight = maxHeight * 0.5f

        // Animate the clipping container's height
        val currentTopHeight by animateDpAsState(
            targetValue = if (isCollapsed) 0.dp else expandedTopHeight,
            animationSpec = tween(durationMillis = 300),
            label = "Top Section Height"
        )

        val topSectionAlpha by animateFloatAsState(
            targetValue = if (isCollapsed) 0f else 1f,
            animationSpec = tween(durationMillis = 200),
            label = "Top Section Alpha"
        )

        Column(modifier = Modifier.fillMaxSize()) {

            // Top Section Container
            if (currentTopHeight > 0.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(currentTopHeight) // This shrinks, acting like a window shade
                        .clipToBounds()
                        .alpha(topSectionAlpha)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(expandedTopHeight)
                    ) {
                        DailyProgressPager(
                            uiState = uiState,
                            dates = weekDates,
                            onDayClick = onNavigateToDailyLog,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.6f)
                        )

                        Text(
                            text = "Your Diets",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.02).em,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.s)
                        )

                        DietList(
                            diets = uiState.diets,
                            onDietClick = onNavigateToDietDetail,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.4f) // 40% of 50% = 20% of total screen
                        )
                    }
                }
            }

            // Bottom Section (Meals)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    text = "Your Meals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.02).em,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.s)
                )

                StaggeredGrid(
                    items = foodCatalog.getAllMeals(),
                    key = { meal -> meal.id },
                    modifier = Modifier.fillMaxSize(),
                    itemContent = { meal ->
                        MealCard(meal = meal)
                    }
                )
            }
        }
    }
}