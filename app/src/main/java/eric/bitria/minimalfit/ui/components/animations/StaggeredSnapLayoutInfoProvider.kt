package eric.bitria.minimalfit.ui.components.animations

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
class StaggeredSnapLayoutInfoProvider(
    private val gridState: LazyStaggeredGridState
) : SnapLayoutInfoProvider {

    override fun calculateSnapOffset(velocity: Float): Float {
        val layoutInfo = gridState.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo
        if (visibleItems.isEmpty()) return 0f

        // 1. Find the item closest to the top
        val closestItem = visibleItems.minByOrNull { abs(it.offset.y) } ?: return 0f

        // 2. Title-Aware Logic:
        // We look for the "Your Meals" title (Index 1).
        // If it's visible and close to the top, we snap to it instead of the cards.
        val titleItem = visibleItems.find { it.index == 1 }

        return if (titleItem != null && abs(titleItem.offset.y) < 250) {
            titleItem.offset.y.toFloat()
        } else {
            closestItem.offset.y.toFloat()
        }
    }
}