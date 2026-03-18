package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import eric.bitria.minimalfit.ui.components.animations.SwipeToDeleteCard
import eric.bitria.minimalfit.ui.components.food.FlexibleHeaderScaffold
import eric.bitria.minimalfit.ui.components.food.actions.AddEntryFab
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

    FlexibleHeaderScaffold(
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
            Text(
                text = meal?.name ?: "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold)
            )
        },
        subtitle = {
            Column {
                Text(
                    text = "${uiState.totalCalories} kcal",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                if (meal != null && meal.description.isNotEmpty()) {
                    Text(
                        text = meal.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
        floatingActionButton = {
            AddEntryFab(
                onClick = { viewModel.openSearchDialog() },
                text = "Add Ingredient"
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
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
                    bottom = innerPadding.calculateBottomPadding() + Spacing.m
                )
            ) {
                if (uiState.ingredients.isNotEmpty()) {
                    items(uiState.ingredients, key = { it.ingredient.id }) { itemState ->
                        SwipeToDeleteCard(
                            onDismiss = { viewModel.deleteIngredient(itemState.ingredient) },
                            modifier = Modifier
                                .padding(horizontal = Spacing.m, vertical = Spacing.xs)
                                .clip(MaterialTheme.shapes.medium)
                                .animateItem(
                                    fadeInSpec = tween(durationMillis = 300),
                                    fadeOutSpec = tween(durationMillis = 300),
                                    placementSpec = tween(durationMillis = 300)
                                )
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
