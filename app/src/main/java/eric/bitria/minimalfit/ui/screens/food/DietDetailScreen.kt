package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
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
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.components.shared.animations.SwipeToDeleteCard
import eric.bitria.minimalfit.ui.components.food.FlexibleTopBar
import eric.bitria.minimalfit.ui.components.food.actions.PrimaryFloatingActionButton
import eric.bitria.minimalfit.ui.components.food.cards.MealCard
import eric.bitria.minimalfit.ui.components.food.dialogs.SearchableItemDialog
import eric.bitria.minimalfit.ui.components.food.dialogs.item.MealItem
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.food.DietDetailViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DietDetailScreen(
    dietId: String,
    onBackClick: () -> Unit,
    onNavigateToMealDetail: (Meal) -> Unit,
    viewModel: DietDetailViewModel = koinViewModel { parametersOf(dietId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val diet = uiState.diet
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var editedName by remember(diet?.name) { mutableStateOf(diet?.name ?: "") }
    var editedDescription by remember(diet?.id) { mutableStateOf(diet?.description ?: "") }
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
                    diet?.let { viewModel.updateDiet(it.copy(imageUrl = editedImageUrl.ifBlank { null })) }
                    showEditImageDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditImageDialog = false }) { Text("Cancel") }
            }
        )
    }

    ScreenConfiguration(
        topBar = {
            FlexibleTopBar(
                backgroundImage = {
                    if (diet != null && !diet.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = diet.imageUrl,
                            contentDescription = diet.name,
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
                            diet?.let { viewModel.updateDiet(it.copy(name = newName)) }
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
                                    text = "Diet name",
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
                    Column {
                        Spacer(Modifier.height(4.dp))
                        BasicTextField(
                            value = editedDescription,
                            onValueChange = { newDesc ->
                                editedDescription = newDesc
                                diet?.let { viewModel.updateDiet(it.copy(description = newDesc)) }
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
                    if (diet != null) {
                        IconButton(
                            onClick = {
                                editedImageUrl = diet.imageUrl ?: ""
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
                text = "Add Meal"
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = false,
        quickActions = false
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        Text(
            text = "Meals",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s)
        )
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.m,
                end = Spacing.m,
                bottom = Spacing.xl * 2
            ),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalItemSpacing = Spacing.m
        ) {
            if (uiState.relatedMeals.isNotEmpty()) {
                items(uiState.relatedMeals, key = { it.meal.id }) { item ->
                    SwipeToDeleteCard(
                        onDismiss = { viewModel.removeMealFromDiet(item.meal.id) },
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraLarge)
                            .animateItem()
                    ) {
                        MealCard(
                            meal = item.meal,
                            calories = item.calories,
                            onClick = { onNavigateToMealDetail(item.meal) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showSearchDialog) {
        SearchableItemDialog(
            title = "Log Meal",
            placeholder = "e.g., Chicken Salad",
            items = uiState.savedMeals,
            itemKey = { it.id },
            filter = { meal, query -> meal.name.contains(query, ignoreCase = true) },
            onDismiss = { viewModel.dismissSearchDialog() },
            itemContent = { meal ->
                MealItem(
                    meal = meal,
                    onAdd = { amount, portionMode ->
                        viewModel.addMeal(mealId = meal.id, amount = amount, portionMode = portionMode)
                        viewModel.dismissSearchDialog()
                    }
                )
            }
        )
    }
}
