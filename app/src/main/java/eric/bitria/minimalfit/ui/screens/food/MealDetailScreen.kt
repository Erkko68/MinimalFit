package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.components.shared.animations.SwipeToDeleteCard
import eric.bitria.minimalfit.ui.components.food.FlexibleTopBar
import eric.bitria.minimalfit.ui.components.food.actions.PrimaryFloatingActionButton
import eric.bitria.minimalfit.ui.components.food.dialogs.SearchableItemDialog
import eric.bitria.minimalfit.ui.components.food.dialogs.item.IngredientItem
import eric.bitria.minimalfit.ui.components.food.lists.IngredientListItem
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.food.MealDetailViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MealDetailScreen(
    mealId: String,
    onBackClick: () -> Unit,
    viewModel: MealDetailViewModel = koinViewModel { parametersOf(mealId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val meal = uiState.meal
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var editedName by remember(meal?.name) { mutableStateOf(meal?.name ?: "") }
    var editedDescription by remember(meal?.id) { mutableStateOf(meal?.description ?: "") }
    var showEditImageDialog by remember { mutableStateOf(false) }
    var editedImageUrl by remember { mutableStateOf("") }

    if (showEditImageDialog) {
        AlertDialog(
            onDismissRequest = { showEditImageDialog = false },
            title = { Text("Edit Image URL") },
            text = {
                OutlinedTextField(
                    value = editedImageUrl,
                    onValueChange = { editedImageUrl = it },
                    label = { Text("Image URL") },
                    placeholder = { Text("https://...") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    meal?.let { viewModel.updateMeal(it.copy(imageUrl = editedImageUrl.ifBlank { null })) }
                    showEditImageDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditImageDialog = false }) { Text("Cancel") }
            }
        )
    }

    ScreenConfiguration(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            FlexibleTopBar(
                backgroundImage = {
                    if (meal != null && !meal.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = meal.imageUrl,
                            contentDescription = meal.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                },
                title = {
                    BasicTextField(
                        value = editedName,
                        onValueChange = { newName ->
                            editedName = newName
                            meal?.let { viewModel.updateMeal(it.copy(name = newName)) }
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        decorationBox = { innerTextField ->
                            if (editedName.isBlank()) {
                                Text(
                                    text = "Meal name",
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            innerTextField()
                        }
                    )
                },
                subtitle = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${uiState.totalCalories} kcal",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        BasicTextField(
                            value = editedDescription,
                            onValueChange = { newDesc ->
                                editedDescription = newDesc
                                meal?.let { viewModel.updateMeal(it.copy(description = newDesc)) }
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant),
                            decorationBox = { innerTextField ->
                                if (editedDescription.isBlank()) {
                                    Text(
                                        text = "Add a description...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                },
                navigationIcon = {
                    FilledIconButton(
                        onClick = onBackClick,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    if (meal != null) {
                        IconButton(
                            onClick = {
                                editedImageUrl = meal.imageUrl ?: ""
                                showEditImageDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit image"
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            PrimaryFloatingActionButton(
                onClick = { viewModel.openSearchDialog() },
                text = "Add Ingredient"
            )
        },
        bottomBar = false,
        quickActions = false
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Ingredients",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = Spacing.xl * 2
            )
        ) {
            if (uiState.ingredients.isNotEmpty()) {
                items(uiState.ingredients, key = { it.ingredient.id }) { itemState ->
                    SwipeToDeleteCard(
                        onDismiss = { viewModel.deleteIngredient(itemState.ingredient) },
                        modifier = Modifier
                            .padding(horizontal = Spacing.m, vertical = Spacing.xs)
                            .clip(MaterialTheme.shapes.medium)
                            .animateItem()
                    ) {
                        IngredientListItem(
                            ingredient = itemState.ingredient,
                            amount = itemState.amount
                        )
                    }
                }
            }
        }
    }

    if (uiState.showSearchDialog) {
        SearchableItemDialog(
            title = "Add Ingredient",
            placeholder = "e.g., Chicken Breast",
            items = uiState.savedIngredients,
            itemKey = { it.id },
            filter = { ingredient, query -> ingredient.name.contains(query, ignoreCase = true) },
            onDismiss = { viewModel.dismissSearchDialog() },
            itemContent = { ingredient ->
                IngredientItem(
                    ingredient = ingredient,
                    onAdd = { amount ->
                        viewModel.addIngredient(ingredient, amount)
                        viewModel.dismissSearchDialog()
                    }
                )
            }
        )
    }
}
