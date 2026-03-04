package eric.bitria.minimalfit.ui.components.food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import eric.bitria.minimalfit.data.model.Nutrient

/**
 * Card-style row:   Calories  [ 5 kcal ]  /  [ 100 g ]
 *
 * Both bracketed values are tappable chips — the first edits the nutrient amount,
 * the second triggers [onUnitAmountClick] to edit the serving size.
 */
@Composable
fun NutritionalField(
    nutrient: Nutrient,
    value: String,
    onValueChange: (String) -> Unit,
    perUnitAmount: String,
    perUnitLabel: String,
    onUnitAmountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var draft by remember(value) { mutableStateOf(value) }

    val hasValue = value.isNotBlank()

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val width = maxWidth

        Surface(
            shape = RoundedCornerShape(12),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = width * 0.04f, vertical = width * 0.025f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nutrient label
                Text(
                    text = nutrient.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                // Value chip — tapping opens edit dialog
                Surface(
                    onClick = { showEditDialog = true },
                    shape = RoundedCornerShape(percent = 50),
                    color = if (hasValue)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = width * 0.03f, vertical = width * 0.012f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(width * 0.01f)
                    ) {
                        Text(
                            text = if (hasValue) value else "—",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (hasValue)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        if (hasValue) {
                            Text(
                                text = nutrient.unit,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )
                        }
                    }
                }

                // Separator
                Text(
                    text = " / ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.padding(horizontal = width * 0.005f)
                )

                // Per-unit amount chip — tapping edits serving size
                Surface(
                    onClick = onUnitAmountClick,
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = width * 0.03f, vertical = width * 0.012f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(width * 0.01f)
                    ) {
                        Text(
                            text = perUnitAmount,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = perUnitLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }

    // Value edit dialog
    if (showEditDialog) {
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        Dialog(onDismissRequest = {
            onValueChange(draft)
            showEditDialog = false
        }) {
            Surface(
                shape = RoundedCornerShape(16),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = nutrient.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it.filter { c -> c.isDigit() || c == '.' } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        suffix = {
                            Text(nutrient.unit, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        shape = RoundedCornerShape(12),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            draft = ""
                            onValueChange("")
                            showEditDialog = false
                        }) { Text("Clear") }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onValueChange(draft)
                                showEditDialog = false
                            },
                            shape = RoundedCornerShape(percent = 50)
                        ) { Text("Set") }
                    }
                }
            }
        }
    }
}
