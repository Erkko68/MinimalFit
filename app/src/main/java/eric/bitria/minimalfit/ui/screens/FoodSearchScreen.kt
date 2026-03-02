package eric.bitria.minimalfit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import eric.bitria.minimalfit.ui.components.food.SavedMealCard
import eric.bitria.minimalfit.ui.viewmodels.FoodViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchScreen(
    onBack: () -> Unit,
    viewModel: FoodViewModel = koinViewModel()
) {
    val savedMeals by viewModel.savedMeals.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()

    var query by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf<String?>(null) }

    val filteredMeals = remember(query, selectedTag, savedMeals) {
        savedMeals.filter { meal ->
            val matchesQuery = query.isBlank() || meal.name.contains(query, ignoreCase = true) ||
                    meal.description.contains(query, ignoreCase = true) ||
                    meal.tags.any { it.contains(query, ignoreCase = true) }
            val matchesTag = selectedTag == null || meal.tags.contains(selectedTag)
            matchesQuery && matchesTag
        }
    }

    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = maxWidth
        val height = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top bar with back + search field
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = width * 0.02f,
                        end = width * 0.05f,
                        top = height * 0.01f,
                        bottom = height * 0.01f
                    )
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search meals…") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
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
                        .weight(1f)
                        .focusRequester(focusRequester)
                )
            }

            // Filter chips row
            LazyRow(
                contentPadding = PaddingValues(horizontal = width * 0.05f),
                horizontalArrangement = Arrangement.spacedBy(width * 0.02f),
                modifier = Modifier.padding(bottom = height * 0.012f)
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

            // Results header
            Text(
                text = "${filteredMeals.size} result${if (filteredMeals.size != 1) "s" else ""}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = width * 0.05f,
                    vertical = height * 0.005f
                )
            )

            // Results list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(height * 0.015f),
                contentPadding = PaddingValues(
                    horizontal = width * 0.05f,
                    vertical = height * 0.01f
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredMeals) { meal ->
                    SavedMealCard(meal)
                }
            }
        }
    }
}

