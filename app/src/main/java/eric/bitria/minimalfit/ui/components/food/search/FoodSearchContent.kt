package eric.bitria.minimalfit.ui.components.food.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import eric.bitria.minimalfit.ui.components.food.cards.SavedMealCard
import eric.bitria.minimalfit.data.model.Meal

/**
 * Reusable search UI: text field + filter chips + results list.
 *
 * When [onMealClick] is provided the results render as selectable [MealPickerRow]s,
 * otherwise they render as expandable [SavedMealCard]s.
 */
@Composable
fun FoodSearchContent(
    savedMeals: List<Meal>,
    availableTags: List<String>,
    modifier: Modifier = Modifier,
    onMealClick: ((Meal) -> Unit)? = null,
    autoFocus: Boolean = false
) {
    var query by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf<String?>(null) }

    val filteredMeals = remember(query, selectedTag, savedMeals) {
        savedMeals.filter { meal ->
            val matchesQuery = query.isBlank() ||
                    meal.name.contains(query, ignoreCase = true) ||
                    meal.description.contains(query, ignoreCase = true) ||
                    meal.tags.any { it.contains(query, ignoreCase = true) }
            val matchesTag = selectedTag == null || meal.tags.contains(selectedTag)
            matchesQuery && matchesTag
        }
    }

    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    if (autoFocus) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }

    BoxWithConstraints(modifier = modifier) {
        val width = maxWidth
        val height = maxHeight

        Column(modifier = Modifier.fillMaxSize()) {
            // Search field
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search meals…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                shape = RoundedCornerShape(percent = 50),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = width * 0.04f, vertical = height * 0.01f)
                    .focusRequester(focusRequester)
            )

            // Filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = width * 0.04f),
                horizontalArrangement = Arrangement.spacedBy(width * 0.02f),
                modifier = Modifier.padding(bottom = height * 0.01f)
            ) {
                item {
                    FilterChip(
                        selected = selectedTag == null,
                        onClick = { selectedTag = null },
                        label = { Text("All") },
                        shape = RoundedCornerShape(percent = 50)
                    )
                }
                items(availableTags) { tag ->
                    FilterChip(
                        selected = selectedTag == tag,
                        onClick = { selectedTag = if (selectedTag == tag) null else tag },
                        label = { Text(tag) },
                        shape = RoundedCornerShape(percent = 50)
                    )
                }
            }

            // Result count
            Text(
                text = "${filteredMeals.size} result${if (filteredMeals.size != 1) "s" else ""}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = width * 0.04f, vertical = height * 0.005f)
            )

            // Results list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(height * 0.012f),
                contentPadding = PaddingValues(horizontal = width * 0.04f, vertical = height * 0.01f),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredMeals) { meal ->
                    if (onMealClick != null) {
                        MealPickerRow(meal = meal, onClick = { onMealClick(meal) })
                    } else {
                        SavedMealCard(meal = meal)
                    }
                }
            }
        }
    }
}

