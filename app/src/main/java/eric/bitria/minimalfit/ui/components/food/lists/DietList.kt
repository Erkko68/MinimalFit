package eric.bitria.minimalfit.ui.components.food.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eric.bitria.minimalfit.data.entity.food.Diet
import eric.bitria.minimalfit.ui.components.food.cards.DietCard
import eric.bitria.minimalfit.ui.theme.Spacing

@Composable
fun DietList(
    diets: List<Diet>,
    onDietClick: (Diet) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        items(diets, key = { it.id }) { diet ->
            DietCard(
                diet = diet,
                onClick = { onDietClick(diet) },
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
            )
        }
    }
}