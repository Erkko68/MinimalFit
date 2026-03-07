package eric.bitria.minimalfit.ui.components.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eric.bitria.minimalfit.ui.viewmodels.Meal

@Composable
fun MealsStaggeredGrid(meals: List<Meal>, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val spacing = maxWidth * 0.04f
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(spacing),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalItemSpacing = spacing
        ) {
            itemsIndexed(meals) { index, meal ->
                MealCard(meal = meal)
            }
        }
    }
}
