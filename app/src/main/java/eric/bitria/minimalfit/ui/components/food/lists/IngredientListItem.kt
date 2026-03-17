package eric.bitria.minimalfit.ui.components.food.lists

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.data.entity.food.Ingredient
import eric.bitria.minimalfit.data.entity.food.MeasurementUnit
import eric.bitria.minimalfit.ui.theme.Spacing

@Composable
fun IngredientListItem(
    ingredient: Ingredient,
    amount: Float,
    modifier: Modifier = Modifier
) {
    val unitLabel = when (ingredient.measurementUnit) {
        MeasurementUnit.GRAMS -> "g"
        MeasurementUnit.MILLILITERS -> "ml"
        MeasurementUnit.PIECE -> "pcs"
    }

    val calories = when (ingredient.measurementUnit) {
        MeasurementUnit.PIECE -> (ingredient.baseCalories * amount).toInt()
        MeasurementUnit.GRAMS, MeasurementUnit.MILLILITERS -> ((ingredient.baseCalories / 100f) * amount).toInt()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = Spacing.m)
                .heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$amount $unitLabel",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "$calories kcal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
