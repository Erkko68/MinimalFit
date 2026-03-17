package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import eric.bitria.minimalfit.ui.components.food.actions.AddEntryFab
import eric.bitria.minimalfit.ui.components.food.dialogs.SearchableItemDialog
import eric.bitria.minimalfit.ui.components.food.dialogs.item.IngredientItem
import eric.bitria.minimalfit.ui.components.food.lists.IngredientListItem
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.food.MealDetailViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MealDetailScreen(
    mealId: String,
    onBackClick: () -> Unit,
    viewModel: MealDetailViewModel = koinViewModel { parametersOf(mealId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val meal = uiState.meal
    val backgroundColor = MaterialTheme.colorScheme.background

    Scaffold(
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            AddEntryFab(
                onClick = { viewModel.openSearchDialog() },
                text = "Add Ingredient"
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
                .background(backgroundColor)
        ) {
            val headerHeight = maxHeight * 0.3f

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(bottom = Spacing.m)
            ) {
                // 1. HEADER SECTION
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(headerHeight)
                    ) {
                        if (!meal?.imageUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = meal.imageUrl,
                                contentDescription = meal.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        0.3f to Color.Transparent,
                                        1.0f to backgroundColor
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomStart)
                                .padding(horizontal = Spacing.m, vertical = Spacing.s)
                        ) {
                            Text(
                                text = meal?.name ?: "",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${uiState.totalCalories} kcal",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 2. DESCRIPTION
                item {
                    Text(
                        text = meal?.description ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s)
                    )
                }

                // 3. INGREDIENTS LIST
                if (uiState.ingredients.isNotEmpty()) {
                    item {
                        Text(
                            text = "Ingredients",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s)
                        )
                    }

                    items(uiState.ingredients) { itemState ->
                        IngredientListItem(
                            ingredient = itemState.ingredient,
                            amount = itemState.amount,
                            modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.xs)
                        )
                    }
                }
            }

            // 4. FIXED BACK BUTTON
            FilledIconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(Spacing.m),
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
