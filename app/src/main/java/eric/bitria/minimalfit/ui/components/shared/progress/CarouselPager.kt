package eric.bitria.minimalfit.ui.components.shared.progress

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.ui.theme.Spacing

@Composable
fun CarouselPager(
    pageCount: Int,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState(initialPage = (pageCount - 1).coerceAtLeast(0), pageCount = { pageCount }),
    onPageClick: ((Int) -> Unit)? = null,
    content: @Composable (Int) -> Unit
) {
    LaunchedEffect(pageCount) {
        if (pageCount > 0) {
            pagerState.scrollToPage(pageCount - 1)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { pageIndex ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.m)
                    .let { base ->
                        if (onPageClick == null) base
                        else base.clickable { onPageClick(pageIndex) }
                    },
                contentAlignment = Alignment.Center
            ) {
                content(pageIndex)
            }
        }

        // Pill indicator row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { iteration ->
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