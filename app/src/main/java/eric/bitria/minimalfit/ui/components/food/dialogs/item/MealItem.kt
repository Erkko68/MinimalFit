package eric.bitria.minimalfit.ui.components.food.dialogs.item

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.food.MeasurementUnit
import eric.bitria.minimalfit.ui.theme.Spacing

@Composable
fun MealItem(
    meal: Meal,
    onAdd: (Float) -> Unit
) {
    var amountText by rememberSaveable {
        mutableStateOf(meal.totalAmount.toString().removeSuffix(".0"))
    }

    val unitSuffix = when (meal.measurementUnit) {
        MeasurementUnit.GRAMS -> "g"
        MeasurementUnit.MILLILITERS -> "ml"
        MeasurementUnit.PIECE -> "pcs"
    }

    val unitLabel = when (meal.measurementUnit) {
        MeasurementUnit.GRAMS -> "100g"
        MeasurementUnit.MILLILITERS -> "100ml"
        MeasurementUnit.PIECE -> "piece"
    }

    val caloriesPer100 = if (meal.totalAmount > 0) {
        (meal.totalCalories / (meal.totalAmount / 100f)).toInt()
    } else 0

    ListItem(
        headlineContent = {
            Text(
                text = meal.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        },
        supportingContent = {
            Text(
                text = "$caloriesPer100 kcal / $unitLabel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                modifier = Modifier.height(40.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .widthIn(min = 70.dp, max = 110.dp)
                        .fillMaxHeight(),
                    suffix = {
                        Text(
                            text = unitSuffix,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                FilledIconButton(
                    onClick = {
                        amountText.toFloatOrNull()?.let { amount ->
                            if (amount > 0) onAdd(amount)
                        }
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add meal",
                        modifier = Modifier.fillMaxSize(0.5f)
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    )
}
