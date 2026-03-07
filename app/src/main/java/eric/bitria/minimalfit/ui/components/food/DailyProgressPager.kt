package eric.bitria.minimalfit.ui.components.food

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import eric.bitria.minimalfit.ui.viewmodels.FoodUiState
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DailyProgressPager(uiState: FoodUiState, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(pageCount = { uiState.weeklyProgress.size })

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.9f),
            contentPadding = PaddingValues(horizontal = 64.dp),
            pageSpacing = 16.dp
        ) { pageIndex ->
            val dailyData = uiState.weeklyProgress[pageIndex]
            val progress = if (dailyData.goalCalories > 0) {
                dailyData.currentCalories.toFloat() / dailyData.goalCalories
            } else 0f

            val pageOffset = (
                    (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                    ).absoluteValue

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp)
                    .graphicsLayer {
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
            ) {
                DailyCalorieCircleCard(dailyData, progress)
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .weight(0.1f)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(uiState.weeklyProgress.size) { iteration ->
                val isSelected = pagerState.currentPage == iteration

                val color by animateColorAsState(
                    targetValue = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    animationSpec = tween(durationMillis = 300),
                    label = "IndicatorColor"
                )

                val indicatorWidth by animateDpAsState(
                    targetValue = if (isSelected) 32.dp else 10.dp,
                    animationSpec = tween(durationMillis = 300),
                    label = "IndicatorWidth"
                )

                Surface(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(10.dp)
                        .width(indicatorWidth),
                    shape = CircleShape,
                    color = color
                ) {}
            }
        }
    }
}