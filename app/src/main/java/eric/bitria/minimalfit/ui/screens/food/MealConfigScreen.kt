package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.data.model.UnitType
import eric.bitria.minimalfit.ui.theme.vividColors
import eric.bitria.minimalfit.ui.viewmodels.FoodViewModel
import org.koin.androidx.compose.koinViewModel

// ── Icon palette ──────────────────────────────────────────────────────────────

private data class NamedIcon(val icon: ImageVector, val label: String)

private val mealIcons = listOf(
    NamedIcon(Icons.Default.BreakfastDining,  "Breakfast"),
    NamedIcon(Icons.Default.LunchDining,       "Lunch"),
    NamedIcon(Icons.Default.DinnerDining,      "Dinner"),
    NamedIcon(Icons.Default.Restaurant,        "Restaurant"),
    NamedIcon(Icons.Default.Egg,               "Egg"),
    NamedIcon(Icons.Default.LocalPizza,        "Pizza"),
    NamedIcon(Icons.Default.LocalCafe,         "Café"),
    NamedIcon(Icons.Default.LocalBar,          "Bar"),
    NamedIcon(Icons.Default.EmojiFoodBeverage, "Beverage"),
    NamedIcon(Icons.Default.LocalDrink,        "Drink"),
    NamedIcon(Icons.Default.Fastfood,          "Fast food"),
    NamedIcon(Icons.Default.BakeryDining,      "Bakery"),
    NamedIcon(Icons.Default.RamenDining,       "Ramen"),
    NamedIcon(Icons.Default.Icecream,         "Ice cream"),
    NamedIcon(Icons.Default.SoupKitchen,       "Soup"),
    NamedIcon(Icons.Default.SetMeal,           "Set meal"),
    NamedIcon(Icons.Default.Blender,           "Smoothie"),
    NamedIcon(Icons.Default.FitnessCenter,     "Fitness"),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MealConfigScreen(
    onBack: () -> Unit,
    viewModel: FoodViewModel = koinViewModel()
) {
    val availableTags by viewModel.availableTags.collectAsState()

    // ── Form state ────────────────────────────────────────────────────────────
    var name        by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var caloriesRaw by remember { mutableStateOf("") }
    var selectedIcon  by remember { mutableStateOf(mealIcons.first().icon) }
    var selectedColor by remember { mutableStateOf(vividColors.first()) }
    var selectedTags  by remember { mutableStateOf(setOf<String>()) }
    var selectedUnit  by remember { mutableStateOf(UnitType.GRAMS) }

    // Tag creation
    var newTagText by remember { mutableStateOf("") }

    // Sheet visibility
    var showIconPicker by remember { mutableStateOf(false) }

    val canSave = name.isNotBlank()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width  = maxWidth
        val height = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = width * 0.01f,
                        top = height * 0.005f,
                        bottom = height * 0.005f,
                        end = width * 0.03f
                    )
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = "New Meal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = width * 0.01f)
                )
                Button(
                    onClick = {
                        if (canSave) {
                            viewModel.createMeal(
                                name        = name.trim(),
                                calories    = caloriesRaw.toIntOrNull() ?: 0,
                                description = description.trim(),
                                tags        = selectedTags.toList(),
                                color       = selectedColor,
                                icon        = selectedIcon,
                                unitType    = selectedUnit
                            )
                            onBack()
                        }
                    },
                    enabled = canSave,
                    shape = RoundedCornerShape(percent = 50),
                ) {
                    Text("Save")
                }
            }

            // ── Scrollable form body ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = width * 0.05f, vertical = height * 0.015f),
                verticalArrangement = Arrangement.spacedBy(height * 0.018f)
            ) {
                // Icon + Color row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(width * 0.05f)
                ) {
                    // Icon preview / picker button
                    Surface(
                        shape = CircleShape,
                        color = selectedColor.copy(alpha = 0.15f),
                        modifier = Modifier
                            .size(72.dp)
                            .clickable { showIconPicker = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = selectedIcon,
                                contentDescription = "Meal icon",
                                tint = selectedColor,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Color palette
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Color",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(vividColors) { color ->
                                val isSelected = color == selectedColor
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .then(
                                            if (isSelected) Modifier.border(
                                                2.dp,
                                                MaterialTheme.colorScheme.onSurface,
                                                CircleShape
                                            ) else Modifier
                                        )
                                        .clickable { selectedColor = color }
                                )
                            }
                        }
                    }
                }

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Calories
                OutlinedTextField(
                    value = caloriesRaw,
                    onValueChange = { caloriesRaw = it.filter(Char::isDigit) },
                    label = { Text("Calories (kcal)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Unit type
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Unit Type",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        UnitType.entries.forEach { unit ->
                            val selected = unit == selectedUnit
                            FilterChip(
                                selected = selected,
                                onClick = { selectedUnit = unit },
                                label = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(unit.label, style = MaterialTheme.typography.labelMedium)
                                        Text(
                                            text = unit.symbol,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (selected)
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // Tags
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        availableTags.forEach { tag ->
                            val isSelected = tag in selectedTags
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedTags = if (isSelected)
                                        selectedTags - tag
                                    else
                                        selectedTags + tag
                                },
                                label = { Text(tag) }
                            )
                        }
                    }

                    // Add new tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newTagText,
                            onValueChange = { newTagText = it },
                            placeholder = { Text("New tag…") },
                            singleLine = true,
                            shape = RoundedCornerShape(percent = 50),
                            modifier = Modifier.weight(1f)
                        )
                        FilledTonalButton(
                            onClick = {
                                val trimmed = newTagText.trim()
                                if (trimmed.isNotBlank()) {
                                    viewModel.addTag(trimmed)
                                    selectedTags = selectedTags + trimmed
                                    newTagText = ""
                                }
                            },
                            shape = RoundedCornerShape(percent = 50),
                            enabled = newTagText.isNotBlank()
                        ) {
                            Text("Add")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(height * 0.05f))
            }
        }

        // ── Icon picker bottom sheet ──────────────────────────────────────────
        if (showIconPicker) {
            ModalBottomSheet(
                onDismissRequest = { showIconPicker = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Text(
                    text = "Choose Icon",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(mealIcons) { namedIcon ->
                        val isSelected = namedIcon.icon == selectedIcon
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable {
                                    selectedIcon = namedIcon.icon
                                    showIconPicker = false
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = namedIcon.icon,
                                    contentDescription = namedIcon.label,
                                    tint = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
}

