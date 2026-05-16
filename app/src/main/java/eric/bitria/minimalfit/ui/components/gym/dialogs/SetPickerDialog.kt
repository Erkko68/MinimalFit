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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
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
import eric.bitria.minimalfit.data.entity.gym.SetType
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
    onConfirm: (
        weight: Float,
        reps: Int,
        type: String,
        durationSeconds: Int,
        preparationSeconds: Int,
        isCompleted: Boolean
    ) -> Unit,
    showCompleted: Boolean = false,
    initialCompleted: Boolean = false,
    type: String = SetType.Reps,
    durationSeconds: Int = 30,
    preparationSeconds: Int = 5
) {
    val wheelHeight = PICKER_ITEM_HEIGHT * PICKER_VISIBLE_ITEMS
    val focusManager = LocalFocusManager.current

    val initialWeightIdx = weightToIndex(weight)
    val weightInList = GYM_WEIGHT_VALUES[initialWeightIdx] == weight

    var selectedType by remember { mutableStateOf(type) }
    var weightIndex by remember { mutableIntStateOf(initialWeightIdx) }
    var repsIndex by remember { mutableIntStateOf((reps - 1).coerceAtLeast(0)) }

    var weightTextMode by remember { mutableStateOf(!weightInList) }
    var repsTextMode by remember { mutableStateOf(reps > 99) }

    var weightText by remember { mutableStateOf(if (weight == 0f) "" else formatWeight(weight)) }
    var repsText by remember { mutableStateOf(if (reps == 0) "" else reps.toString()) }
    var durationText by remember { mutableStateOf(durationSeconds.coerceAtLeast(1).toString()) }
    var prepText by remember { mutableStateOf(preparationSeconds.coerceAtLeast(0).toString()) }
    var isCompleted by remember { mutableStateOf(initialCompleted) }

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

                Spacer(modifier = Modifier.height(Spacing.m))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.CenterHorizontally)
                ) {
                    FilterChip(
                        selected = selectedType == SetType.Reps,
                        onClick = { selectedType = SetType.Reps },
                        label = { Text("Reps") }
                    )
                    FilterChip(
                        selected = selectedType == SetType.Timed,
                        onClick = { selectedType = SetType.Timed },
                        label = { Text("Timed") }
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.m))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    WeightPicker(
                        wheelHeight = wheelHeight,
                        weightTextMode = weightTextMode,
                        weightText = weightText,
                        weightIndex = weightIndex,
                        weightFocus = weightFocus,
                        weightEverFocused = weightEverFocused,
                        onTextModeChange = { weightTextMode = it },
                        onTextChange = { weightText = it },
                        onIndexChange = { weightIndex = it },
                        onEverFocusedChange = { weightEverFocused = it },
                        focusManager = focusManager
                    )

                    Box(
                        modifier = Modifier
                            .height(wheelHeight)
                            .padding(horizontal = Spacing.m),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedType == SetType.Timed) "for" else "x",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (selectedType == SetType.Timed) {
                        TimedNumberField(
                            value = durationText,
                            onValueChange = { durationText = it.toSecondsInput(maxDigits = 3) },
                            label = "sec",
                            modifier = Modifier.width(96.dp)
                        )
                    } else {
                        RepsPicker(
                            wheelHeight = wheelHeight,
                            repsTextMode = repsTextMode,
                            repsText = repsText,
                            repsIndex = repsIndex,
                            repsFocus = repsFocus,
                            repsEverFocused = repsEverFocused,
                            onTextModeChange = { repsTextMode = it },
                            onTextChange = { repsText = it },
                            onIndexChange = { repsIndex = it },
                            onEverFocusedChange = { repsEverFocused = it },
                            focusManager = focusManager
                        )
                    }
                }

                if (selectedType == SetType.Timed) {
                    Spacer(modifier = Modifier.height(Spacing.m))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Prep",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = Spacing.m)
                        )
                        TimedNumberField(
                            value = prepText,
                            onValueChange = { prepText = it.toSecondsInput(maxDigits = 2) },
                            label = "sec",
                            modifier = Modifier.width(88.dp)
                        )
                    }
                }

                if (showCompleted) {
                    Spacer(modifier = Modifier.height(Spacing.m))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Checkbox(
                            checked = isCompleted,
                            onCheckedChange = { isCompleted = it }
                        )
                        Text(
                            text = "Mark as completed",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = Spacing.xs)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.m))

                Button(
                    onClick = {
                        val finalWeight = if (weightTextMode) {
                            weightText.replace(',', '.').toFloatOrNull() ?: GYM_WEIGHT_VALUES[weightIndex]
                        } else {
                            GYM_WEIGHT_VALUES[weightIndex]
                        }
                        val finalReps = if (repsTextMode) {
                            repsText.toIntOrNull()?.coerceAtLeast(1) ?: (repsIndex + 1)
                        } else {
                            repsIndex + 1
                        }
                        onConfirm(
                            finalWeight,
                            if (selectedType == SetType.Timed) 0 else finalReps,
                            selectedType,
                            if (selectedType == SetType.Timed) durationText.toIntOrNull()?.coerceAtLeast(1) ?: 30 else 0,
                            if (selectedType == SetType.Timed) prepText.toIntOrNull()?.coerceAtLeast(0) ?: 5 else 5,
                            isCompleted
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

@Composable
private fun WeightPicker(
    wheelHeight: androidx.compose.ui.unit.Dp,
    weightTextMode: Boolean,
    weightText: String,
    weightIndex: Int,
    weightFocus: FocusRequester,
    weightEverFocused: Boolean,
    onTextModeChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onIndexChange: (Int) -> Unit,
    onEverFocusedChange: (Boolean) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.width(100.dp).height(wheelHeight)) {
            if (weightTextMode) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { onTextChange(it.toWeightInput()) },
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
                                onEverFocusedChange(true)
                            } else if (weightEverFocused) {
                                val parsed = weightText.replace(',', '.').toFloatOrNull()
                                if (parsed != null) {
                                    val idx = weightToIndex(parsed)
                                    if (GYM_WEIGHT_VALUES[idx] == parsed) {
                                        onIndexChange(idx)
                                        onTextModeChange(false)
                                    }
                                }
                            }
                        }
                )
            } else {
                WheelPicker(
                    count = GYM_WEIGHT_VALUES.size,
                    initialIndex = weightIndex,
                    onIndexChanged = onIndexChange,
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
                            onTextChange(formatWeight(GYM_WEIGHT_VALUES[weightIndex]))
                            onTextModeChange(true)
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
}

@Composable
private fun RepsPicker(
    wheelHeight: androidx.compose.ui.unit.Dp,
    repsTextMode: Boolean,
    repsText: String,
    repsIndex: Int,
    repsFocus: FocusRequester,
    repsEverFocused: Boolean,
    onTextModeChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onIndexChange: (Int) -> Unit,
    onEverFocusedChange: (Boolean) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.width(80.dp).height(wheelHeight)) {
            if (repsTextMode) {
                OutlinedTextField(
                    value = repsText,
                    onValueChange = { onTextChange(it.filter { c -> c.isDigit() }.take(3)) },
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
                                onEverFocusedChange(true)
                            } else if (repsEverFocused) {
                                val parsed = repsText.toIntOrNull()
                                if (parsed != null && parsed in 1..99) {
                                    onIndexChange(parsed - 1)
                                    onTextModeChange(false)
                                }
                            }
                        }
                )
            } else {
                WheelPicker(
                    count = 99,
                    initialIndex = repsIndex,
                    onIndexChanged = onIndexChange,
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
                            onTextChange((repsIndex + 1).toString())
                            onTextModeChange(true)
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

@Composable
private fun TimedNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            modifier = modifier
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

internal fun String.toSecondsInput(maxDigits: Int): String =
    filter { it.isDigit() }.take(maxDigits)

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
