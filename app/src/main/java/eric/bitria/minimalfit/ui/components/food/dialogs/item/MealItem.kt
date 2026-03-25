package eric.bitria.minimalfit.ui.components.food.dialogs.item

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.data.entity.food.MeasurementUnit
import eric.bitria.minimalfit.ui.theme.Dimensions
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.food.MealPortionMode

@Composable
fun MealItem(
    meal: Meal,
    onAdd: (Float, MealPortionMode) -> Unit
) {
    var portionMode by rememberSaveable { mutableStateOf(MealPortionMode.WEIGHT) }

    val initialAmount = rememberSaveable(meal.measurementUnit, portionMode) {
        if (portionMode == MealPortionMode.FULL_MEAL) "1"
        else if (meal.measurementUnit == MeasurementUnit.PIECE) "1" else "100"
    }

    var amountText by rememberSaveable(portionMode) { mutableStateOf(initialAmount) }

    val unitSuffix = when (portionMode) {
        MealPortionMode.FULL_MEAL -> "meal"
        MealPortionMode.WEIGHT -> when (meal.measurementUnit) {
            MeasurementUnit.GRAMS -> "g"
            MeasurementUnit.MILLILITERS -> "ml"
            MeasurementUnit.PIECE -> "pcs"
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimensions.listItemHeight)
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

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
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

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            modifier = Modifier.weight(0.45f),
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
                val selectedAmount = amountText.toFloatOrNull() ?: 0f
                if (selectedAmount > 0f) onAdd(selectedAmount, portionMode)
            },
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
