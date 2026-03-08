package eric.bitria.minimalfit.ui.components.food.progress

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import eric.bitria.minimalfit.ui.components.food.cards.DailyCalorieCircleCard
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.FoodUiState
import java.time.LocalDate
import kotlin.math.absoluteValue

@Composable
fun DailyProgressPager(
    uiState: FoodUiState,
    dates: List<LocalDate>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        initialPage = (uiState.weeklyProgress.size - 1).coerceAtLeast(0),
        pageCount = { uiState.weeklyProgress.size }
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = Spacing.xxxl),
            pageSpacing = Spacing.m
        ) { pageIndex ->
            val dailyData = uiState.weeklyProgress[pageIndex]
            val progress = if (dailyData.goalCalories > 0) {
                dailyData.currentCalories.toFloat() / dailyData.goalCalories
            } else 0f

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = Spacing.m)
                    .graphicsLayer {
                        val pageOffset = (
                                (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                                ).absoluteValue
                        val scale = lerp(
                            start = 0.85f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        scaleX = scale
                        scaleY = scale
                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onDayClick(dates[pageIndex]) }
                    )
            ) {
                DailyCalorieCircleCard(dailyData, progress)
            }
        }

        // Pill indicator row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.m),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(uiState.weeklyProgress.size) { iteration ->
                val isSelected = pagerState.currentPage == iteration

                val color by animateColorAsState(
                    targetValue = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    animationSpec = tween(durationMillis = 300),
                    label = "IndicatorColor"
                )

                val indicatorWidth by animateDpAsState(
                    targetValue = if (isSelected) 28.dp else 8.dp,
                    animationSpec = tween(durationMillis = 300),
                    label = "IndicatorWidth"
                )

                Surface(
                    modifier = Modifier
                        .padding(horizontal = Spacing.xs)
                        .height(8.dp)
                        .width(indicatorWidth),
                    shape = CircleShape,
                    color = color
                ) {}
            }
        }
    }
}