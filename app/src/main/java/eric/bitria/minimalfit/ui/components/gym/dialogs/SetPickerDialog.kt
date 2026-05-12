package eric.bitria.minimalfit.ui.components.gym.dialogs

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import eric.bitria.minimalfit.ui.components.shared.WheelPicker
import eric.bitria.minimalfit.ui.theme.Dimensions
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.util.GYM_WEIGHT_VALUES
import eric.bitria.minimalfit.util.formatWeight
import eric.bitria.minimalfit.util.weightToIndex

private val PICKER_ITEM_HEIGHT = Dimensions.WheelPicker.itemHeight
private const val PICKER_VISIBLE_ITEMS = Dimensions.WheelPicker.visibleItems

@Composable
fun SetPickerDialog(
    title: String,
    weight: Float,
    reps: Int,
    onDismiss: () -> Unit,
    onConfirm: (weight: Float, reps: Int) -> Unit,
    extraContent: @Composable () -> Unit = {}
) {
    val wheelHeight = PICKER_ITEM_HEIGHT * PICKER_VISIBLE_ITEMS
    val focusManager = LocalFocusManager.current

    val initialWeightIdx = weightToIndex(weight)
    val weightInList = GYM_WEIGHT_VALUES[initialWeightIdx] == weight

    var weightIndex by remember { mutableIntStateOf(initialWeightIdx) }
    var repsIndex by remember { mutableIntStateOf((reps - 1).coerceAtLeast(0)) }

    var weightTextMode by remember { mutableStateOf(!weightInList) }
    var repsTextMode by remember { mutableStateOf(reps > 99) }

    var weightText by remember { mutableStateOf(if (weight == 0f) "" else formatWeight(weight)) }
    var repsText by remember { mutableStateOf(if (reps == 0) "" else reps.toString()) }

    // Keyed on the text-mode flag so it resets to false in the same recomposition that
    // activates text mode — before focus resolution fires onFocusChanged(false) on the
    // newly-composed TextField. A plain remember {} would hold the stale true value from
    // the previous text-mode session and immediately trigger the revert-to-wheel logic.
    var weightEverFocused by remember(weightTextMode) { mutableStateOf(false) }
    var repsEverFocused by remember(repsTextMode) { mutableStateOf(false) }

    val weightFocus = remember { FocusRequester() }
    val repsFocus = remember { FocusRequester() }

    LaunchedEffect(weightTextMode) { if (weightTextMode) weightFocus.requestFocus() }
    LaunchedEffect(repsTextMode) { if (repsTextMode) repsFocus.requestFocus() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.l)
                    // Clear focus when tapping any non-interactive area inside the dialog.
                    // Final pass fires after children — if a child (e.g. TextField, Button) consumed
                    // the event, isConsumed is true and we skip clearFocus, so typing in the
                    // TextField or clicking Done never accidentally reverts to wheel mode.
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Final)
                                if (event.type == PointerEventType.Press &&
                                    event.changes.all { !it.isConsumed }
                                ) {
                                    focusManager.clearFocus()
                                }
                            }
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.l))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Weight column
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.width(100.dp).height(wheelHeight)) {
                            if (weightTextMode) {
                                OutlinedTextField(
                                    value = weightText,
                                    onValueChange = { weightText = it.toWeightInput() },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                        .focusRequester(weightFocus)
                                        .onFocusChanged { fs ->
                                            if (fs.isFocused) {
                                                weightEverFocused = true
                                            } else if (weightEverFocused) {
                                                val parsed = weightText.replace(',', '.').toFloatOrNull()
                                                if (parsed != null) {
                                                    val idx = weightToIndex(parsed)
                                                    if (GYM_WEIGHT_VALUES[idx] == parsed) {
                                                        weightIndex = idx
                                                        weightTextMode = false
                                                    }
                                                }
                                            }
                                        }
                                )
                            } else {
                                WheelPicker(
                                    count = GYM_WEIGHT_VALUES.size,
                                    initialIndex = weightIndex,
                                    onIndexChanged = { weightIndex = it },
                                    visibleItems = PICKER_VISIBLE_ITEMS,
                                    itemHeight = PICKER_ITEM_HEIGHT,
                                    modifier = Modifier.fillMaxWidth()
                                ) { i, isSelected ->
                                    Text(
                                        text = formatWeight(GYM_WEIGHT_VALUES[i]),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                                                else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(PICKER_ITEM_HEIGHT)
                                        .align(Alignment.Center)
                                        .clickable {
                                            weightText = formatWeight(GYM_WEIGHT_VALUES[weightIndex])
                                            weightTextMode = true
                                        }
                                )
                            }
                        }
                        Text(
                            text = "kg",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier.height(wheelHeight).padding(horizontal = Spacing.m),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "×",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Reps column
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.width(80.dp).height(wheelHeight)) {
                            if (repsTextMode) {
                                OutlinedTextField(
                                    value = repsText,
                                    onValueChange = { repsText = it.filter { c -> c.isDigit() }.take(3) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                        .focusRequester(repsFocus)
                                        .onFocusChanged { fs ->
                                            if (fs.isFocused) {
                                                repsEverFocused = true
                                            } else if (repsEverFocused) {
                                                val parsed = repsText.toIntOrNull()
                                                if (parsed != null && parsed in 1..99) {
                                                    repsIndex = parsed - 1
                                                    repsTextMode = false
                                                }
                                            }
                                        }
                                )
                            } else {
                                WheelPicker(
                                    count = 99,
                                    initialIndex = repsIndex,
                                    onIndexChanged = { repsIndex = it },
                                    visibleItems = PICKER_VISIBLE_ITEMS,
                                    itemHeight = PICKER_ITEM_HEIGHT,
                                    modifier = Modifier.fillMaxWidth()
                                ) { i, isSelected ->
                                    Text(
                                        text = "${i + 1}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                                                else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(PICKER_ITEM_HEIGHT)
                                        .align(Alignment.Center)
                                        .clickable {
                                            repsText = (repsIndex + 1).toString()
                                            repsTextMode = true
                                        }
                                )
                            }
                        }
                        Text(
                            text = "reps",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                extraContent()

                Spacer(modifier = Modifier.height(Spacing.m))

                Button(
                    onClick = {
                        val finalWeight = if (weightTextMode)
                            weightText.replace(',', '.').toFloatOrNull() ?: GYM_WEIGHT_VALUES[weightIndex]
                        else
                            GYM_WEIGHT_VALUES[weightIndex]
                        val finalReps = if (repsTextMode)
                            repsText.toIntOrNull()?.coerceAtLeast(1) ?: (repsIndex + 1)
                        else
                            repsIndex + 1
                        onConfirm(finalWeight, finalReps)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}

internal fun String.toWeightInput(): String {
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
    return if (parts.size == 2) "${parts[0].take(3)}.${parts[1].take(2)}" else parts[0].take(3)
}
