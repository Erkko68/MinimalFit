package eric.bitria.minimalfit.ui.components.food.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eric.bitria.minimalfit.data.model.Meal
import eric.bitria.minimalfit.ui.theme.Spacing

@Composable
fun StaggeredGrid(
    meals: List<Meal>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (Meal) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        verticalItemSpacing = Spacing.m
    ) {
        itemsIndexed(meals) { _, meal ->
            itemContent(meal)
        }
    }
}
