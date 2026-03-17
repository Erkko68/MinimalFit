package eric.bitria.minimalfit.ui.components.food.dialogs.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.food.MeasurementUnit
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.food.MealPortionMode

@Composable
fun MealItem(
    meal: Meal,
    onAdd: (Float, MealPortionMode) -> Unit
) {
    var amountText by rememberSaveable { mutableStateOf("100") }
    var portionMode by rememberSaveable { mutableStateOf(MealPortionMode.WEIGHT) }

    val unitSuffix = when (meal.measurementUnit) {
        MeasurementUnit.GRAMS -> "g"
        MeasurementUnit.MILLILITERS -> "ml"
        MeasurementUnit.PIECE -> "pcs"
    }

    val defaultWeight = 100f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = Spacing.m, vertical = Spacing.s)
    ) {
        // Meal info - Takes up all available space
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = meal.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                FilterChip(
                    selected = portionMode == MealPortionMode.WEIGHT,
                    onClick = { portionMode = MealPortionMode.WEIGHT },
                    label = { Text("Weight") },
                    colors = FilterChipDefaults.filterChipColors()
                )
                FilterChip(
                    selected = portionMode == MealPortionMode.FULL_MEAL,
                    onClick = { portionMode = MealPortionMode.FULL_MEAL },
                    label = { Text("Full meal") },
                    colors = FilterChipDefaults.filterChipColors()
                )
            }
        }

        // Amount input + add button - Compact layout
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            modifier = Modifier.fillMaxHeight()
        ) {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                modifier = Modifier.width(84.dp),
                enabled = portionMode == MealPortionMode.WEIGHT,
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
                    val selectedAmount = if (portionMode == MealPortionMode.FULL_MEAL) {
                        defaultWeight
                    } else {
                        amountText.toFloatOrNull() ?: 0f
                    }
                    if (selectedAmount > 0f) onAdd(selectedAmount, portionMode)
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = Spacing.xs)
                    .aspectRatio(1f),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add meal",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
