package eric.bitria.minimalfit.ui.components.food.dialogs.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import eric.bitria.minimalfit.data.entity.food.Ingredient
import eric.bitria.minimalfit.data.entity.food.MeasurementUnit
import eric.bitria.minimalfit.ui.theme.Dimensions
import eric.bitria.minimalfit.ui.theme.Spacing

@Composable
fun IngredientItem(
    ingredient: Ingredient,
    onAdd: (Float) -> Unit
) {
    val initialAmount = remember(ingredient.measurementUnit) {
        if (ingredient.measurementUnit == MeasurementUnit.PIECE) "1" else "100"
    }
    var amountText by remember { mutableStateOf(initialAmount) }

    val unitSuffix = when (ingredient.measurementUnit) {
        MeasurementUnit.GRAMS -> "g"
        MeasurementUnit.MILLILITERS -> "ml"
        MeasurementUnit.PIECE -> "pcs"
    }

    val unitLabel = when (ingredient.measurementUnit) {
        MeasurementUnit.GRAMS -> "100g"
        MeasurementUnit.MILLILITERS -> "100ml"
        MeasurementUnit.PIECE -> "piece"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimensions.listItemHeight)
    ) {
        // Ingredient info - Takes up all available space
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "${ingredient.baseCalories} kcal / $unitLabel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            modifier = Modifier.weight(0.4f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            suffix = {
                Text(
                    text = unitSuffix,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        FilledIconButton(
            onClick = {
                amountText.toFloatOrNull()?.let { amount ->
                    if (amount > 0) onAdd(amount)
                }
            },
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add ingredient",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
