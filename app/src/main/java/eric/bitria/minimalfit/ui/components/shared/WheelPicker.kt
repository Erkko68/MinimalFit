package eric.bitria.minimalfit.ui.components.shared

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * A vertically scrolling drum-roll picker that snaps to a single item.
 *
 * @param count          Total number of selectable items.
 * @param initialIndex   Item to centre on first composition.
 * @param onIndexChanged Called for every item that becomes centred while scrolling.
 * @param onSettled      Called once when the wheel stops moving. Prefer this over
 *                       [onIndexChanged] for side-effects like DB writes.
 * @param visibleItems   How many rows to show at once — odd numbers look best.
 * @param itemHeight     Height of each row.
 * @param itemContent    Composable for each row; receives the real index and
 *                       whether it is currently selected (centred).
 */
@Composable
fun WheelPicker(
    count: Int,
    initialIndex: Int = 0,
    onIndexChanged: (Int) -> Unit = {},
    onSettled: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    visibleItems: Int = 5,
    itemHeight: Dp = 44.dp,
    itemContent: @Composable (index: Int, isSelected: Boolean) -> Unit
) {
    // Padding rows let items at either end be centred without contentPadding,
    // which would break the initialFirstVisibleItemIndex calculation.
    val paddingRows = visibleItems / 2

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialIndex
    )
    val flingBehavior = rememberSnapFlingBehavior(listState)

    val selectedIndex by remember(listState) {
        derivedStateOf {
            val info = listState.layoutInfo
            val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2f
            val paddedIndex = info.visibleItemsInfo
                .minByOrNull { abs(it.offset + it.size / 2f - viewportCenter) }
                ?.index ?: (initialIndex + paddingRows)
            (paddedIndex - paddingRows).coerceIn(0, count - 1)
        }
    }

    LaunchedEffect(selectedIndex) {
        onIndexChanged(selectedIndex)
    }

    // Fire onSettled once scrolling has stopped — avoids spamming on every scrolled frame.
    var hasScrolled by remember { mutableStateOf(false) }
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            hasScrolled = true
        } else if (hasScrolled) {
            onSettled(selectedIndex)
        }
    }

    Box(
        modifier = modifier.height(itemHeight * visibleItems),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {}

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.height(itemHeight * visibleItems)
        ) {
            items(paddingRows) {
                Box(modifier = Modifier.fillMaxWidth().height(itemHeight))
            }
            items(count) { index ->
                val distance = abs(index - selectedIndex)
                val alpha = when (distance) {
                    0 -> 1f
                    1 -> 0.55f
                    2 -> 0.25f
                    else -> 0.08f
                }
                val scale = when (distance) {
                    0 -> 1f
                    1 -> 0.88f
                    else -> 0.78f
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .alpha(alpha)
                        .scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    itemContent(index, index == selectedIndex)
                }
            }
            items(paddingRows) {
                Box(modifier = Modifier.fillMaxWidth().height(itemHeight))
            }
        }
    }
}
