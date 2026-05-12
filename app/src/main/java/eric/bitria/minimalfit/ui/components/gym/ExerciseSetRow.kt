package eric.bitria.minimalfit.ui.components.gym

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.util.formatWeight

@Composable
fun ExerciseSetRow(
    index: Int,
    set: Set,
    onUpdate: (Set) -> Unit,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }

    val containerColor = MaterialTheme.colorScheme.surfaceVariant
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        modifier = modifier
            .fillMaxWidth()
            .then(if (isActive) Modifier.clickable { showEditDialog = true } else Modifier)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = Spacing.m, vertical = Spacing.s)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor.copy(alpha = 0.5f),
                modifier = Modifier.width(Spacing.m)
            )

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = if (set.weight == 0f) "-" else formatWeight(set.weight),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
            }

            VerticalDivider(
                modifier = Modifier.padding(vertical = Spacing.xs),
                color = contentColor.copy(alpha = 0.2f)
            )

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = if (set.reps == 0) "-" else set.reps.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
            }

            Checkbox(
                checked = set.isCompleted,
                onCheckedChange = { checked ->
                    if (isActive) onUpdate(set.copy(isCompleted = checked))
                },
                enabled = isActive
            )
        }
    }

    if (showEditDialog) {
        SetEditDialog(
            set = set,
            index = index,
            onDismiss = { showEditDialog = false },
            onConfirm = { updated ->
                onUpdate(updated)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun SetEditDialog(
    set: Set,
    index: Int,
    onDismiss: () -> Unit,
    onConfirm: (Set) -> Unit
) {
    var weightText by remember(set.id, set.weight) {
        mutableStateOf(if (set.weight == 0f) "" else formatWeight(set.weight))
    }
    var repsText by remember(set.id, set.reps) {
        mutableStateOf(if (set.reps == 0) "" else set.reps.toString())
    }
    var isCompleted by remember(set.id, set.isCompleted) {
        mutableStateOf(set.isCompleted)
    }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val parsedWeight = weightText.replace(',', '.').toFloatOrNull()
    val parsedReps = repsText.toIntOrNull()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.l),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Set ${index + 1}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.l))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { value -> weightText = value.toWeightInput() },
                        label = { Text("Weight") },
                        suffix = { Text("kg") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                    )

                    OutlinedTextField(
                        value = repsText,
                        onValueChange = { value ->
                            repsText = value.filter { it.isDigit() }.take(3)
                        },
                        label = { Text("Reps") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { isCompleted = it }
                    )
                    Text("Mark set as completed")
                }

                Spacer(modifier = Modifier.height(Spacing.l))

                Button(
                    enabled = parsedWeight != null || parsedReps != null,
                    onClick = {
                        onConfirm(
                            set.copy(
                                weight = parsedWeight ?: 0f,
                                reps = parsedReps ?: 0,
                                isCompleted = isCompleted
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}

private fun String.toWeightInput(): String {
    val normalized = replace(',', '.')
    val builder = StringBuilder()
    var hasDecimalSeparator = false

    normalized.forEach { char ->
        when {
            char.isDigit() -> builder.append(char)
            char == '.' && !hasDecimalSeparator -> {
                builder.append(char)
                hasDecimalSeparator = true
            }
        }
    }

    val parts = builder.toString().split('.', limit = 2)
    return if (parts.size == 2) {
        "${parts[0].take(3)}.${parts[1].take(2)}"
    } else {
        parts[0].take(3)
    }
}
