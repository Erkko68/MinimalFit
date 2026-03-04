package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import eric.bitria.minimalfit.data.model.UnitType
import eric.bitria.minimalfit.ui.components.food.NutritionalField
import eric.bitria.minimalfit.ui.components.food.dialog.IconColorPickerDialog
import eric.bitria.minimalfit.ui.components.food.dialog.TagPickerDialog
import eric.bitria.minimalfit.ui.components.food.mealIcons
import eric.bitria.minimalfit.ui.theme.vividColors
import eric.bitria.minimalfit.ui.viewmodels.FoodViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MealConfigScreen(
    onBack: () -> Unit,
    viewModel: FoodViewModel = koinViewModel()
) {
    val availableTags by viewModel.availableTags.collectAsState()

    // ── Form state ────────────────────────────────────────────────────────────
    var name          by remember { mutableStateOf("") }
    var selectedIcon  by remember { mutableStateOf(mealIcons.first().icon) }
    var selectedColor by remember { mutableStateOf(vividColors.first()) }
    var selectedTags  by remember { mutableStateOf(setOf<String>()) }
    var selectedUnit  by remember { mutableStateOf(UnitType.GRAMS) }

    var nutritionValues by remember {
        mutableStateOf(Nutrient.entries.associateWith { "" })
    }

    // Serving size — editable amount the nutrition values are based on (e.g. "100")
    var servingSizeRaw by remember { mutableStateOf("100") }

    var showIconColorPicker  by remember { mutableStateOf(false) }
    var showTagPicker        by remember { mutableStateOf(false) }
    var showUnitDropdown     by remember { mutableStateOf(false) }
    var showServingSizeDialog by remember { mutableStateOf(false) }

    val canSave = name.isNotBlank()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width  = maxWidth
        val height = maxHeight

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Colored header ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.22f)
                    .background(selectedColor.copy(alpha = 0.12f))
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(width * 0.02f)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = selectedColor
                    )
                }

                // Icon — no background sphere
                Icon(
                    imageVector = selectedIcon,
                    contentDescription = "Meal icon",
                    tint = selectedColor,
                    modifier = Modifier
                        .size(width * 0.16f)
                        .align(Alignment.Center)
                )

                // Name as colored title at bottom-start
                Text(
                    text = name.ifBlank { "New Meal" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = selectedColor,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = width * 0.05f, bottom = height * 0.015f)
                )

                // Edit appearance — bottom-end corner
                IconButton(
                    onClick = { showIconColorPicker = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(width * 0.03f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit appearance",
                        tint = selectedColor
                    )
                }
            }

            // ── Fixed section: tags + nutrition header ────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = width * 0.05f)
                    .padding(top = height * 0.018f),
                verticalArrangement = Arrangement.spacedBy(height * 0.012f)
            ) {
                // Tags — single compact row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(width * 0.02f)
                ) {
                    if (selectedTags.isEmpty()) {
                        Text(
                            text = "No tags selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Scrollable tag chips inline
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(width * 0.02f),
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedTags.forEach { tag ->
                                AssistChip(
                                    onClick = { selectedTags = selectedTags - tag },
                                    label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove $tag",
                                            modifier = Modifier.size(width * 0.03f)
                                        )
                                    },
                                    shape = RoundedCornerShape(percent = 50)
                                )
                            }
                        }
                    }
                    FilledTonalIconButton(
                        onClick = { showTagPicker = true },
                        modifier = Modifier.size(width * 0.08f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Manage tags",
                            modifier = Modifier.size(width * 0.042f)
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                // Nutrition title + unit chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nutrition",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Box {
                        AssistChip(
                            onClick = { showUnitDropdown = true },
                            label = {
                                Text(
                                    "per ${servingSizeRaw.ifBlank { "100" }} ${selectedUnit.symbol}",
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(width * 0.04f)
                                )
                            },
                            shape = RoundedCornerShape(percent = 50)
                        )
                        DropdownMenu(
                            expanded = showUnitDropdown,
                            onDismissRequest = { showUnitDropdown = false }
                        ) {
                            UnitType.entries.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text("${unit.label} (${unit.symbol})") },
                                    onClick = {
                                        selectedUnit = unit
                                        showUnitDropdown = false
                                    },
                                    trailingIcon = {
                                        if (unit == selectedUnit) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ── Scrollable nutrient list ──────────────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = width * 0.05f,
                    vertical = height * 0.012f
                ),
                verticalArrangement = Arrangement.spacedBy(height * 0.01f)
            ) {
                items(Nutrient.entries) { nutrient ->
                    NutritionalField(
                        nutrient = nutrient,
                        value = nutritionValues[nutrient] ?: "",
                        onValueChange = { newVal ->
                            nutritionValues = nutritionValues + (nutrient to newVal)
                        },
                        perUnitAmount = servingSizeRaw.ifBlank { "100" },
                        perUnitLabel = selectedUnit.symbol,
                        onUnitAmountClick = { showServingSizeDialog = true }
                    )
                }
                item { Spacer(Modifier.height(height * 0.01f)) }
            }

            // ── Bottom save bar ───────────────────────────────────────────────
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (canSave) {
                            val nutrition = nutritionValues
                                .mapValues { (_, v) -> v.toFloatOrNull() }
                                .filterValues { it != null && it > 0f }
                                .mapValues { (_, v) -> v!! }
                            viewModel.createMeal(
                                name = name.trim(),
                                description = "",
                                tags = selectedTags.toList(),
                                color = selectedColor,
                                icon = selectedIcon,
                                unitType = selectedUnit,
                                servingSize = servingSizeRaw.toFloatOrNull() ?: 100f,
                                nutrition = nutrition
                            )
                            onBack()
                        }
                    },
                    enabled = canSave,
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(containerColor = selectedColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = width * 0.05f, vertical = height * 0.012f)
                        .navigationBarsPadding()
                ) {
                    Text(
                        text = "Save Meal",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    if (showIconColorPicker) {
        IconColorPickerDialog(
            currentIcon = selectedIcon,
            currentColor = selectedColor,
            currentName = name,
            onDismiss = { showIconColorPicker = false },
            onConfirm = { icon, color, newName ->
                selectedIcon = icon
                selectedColor = color
                name = newName
                showIconColorPicker = false
            }
        )
    }

    if (showTagPicker) {
        TagPickerDialog(
            availableTags = availableTags,
            selectedTags = selectedTags,
            onDismiss = { showTagPicker = false },
            onConfirm = { tags ->
                selectedTags = tags
                showTagPicker = false
            },
            onCreateTag = { tag -> viewModel.addTag(tag) }
        )
    }

    // Serving size edit dialog
    if (showServingSizeDialog) {
        var servingDraft by remember { mutableStateOf(servingSizeRaw) }
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        Dialog(onDismissRequest = {
            servingSizeRaw = servingDraft.ifBlank { "100" }
            showServingSizeDialog = false
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
                        text = "Serving size",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = servingDraft,
                        onValueChange = { servingDraft = it.filter { c -> c.isDigit() || c == '.' } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        suffix = {
                            Text(
                                selectedUnit.symbol,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                        TextButton(onClick = { showServingSizeDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                servingSizeRaw = servingDraft.ifBlank { "100" }
                                showServingSizeDialog = false
                            },
                            shape = RoundedCornerShape(percent = 50)
                        ) { Text("Set") }
                    }
                }
            }
        }
    }
}
