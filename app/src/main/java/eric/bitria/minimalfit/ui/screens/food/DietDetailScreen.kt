package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
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
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.ui.components.food.actions.AddEntryFab
import eric.bitria.minimalfit.ui.components.food.cards.MealCard
import eric.bitria.minimalfit.ui.components.food.dialogs.SearchableItemDialog
import eric.bitria.minimalfit.ui.components.food.dialogs.item.MealItem
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.food.DietDetailViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DietDetailScreen(
    dietId: String,
    onBackClick: () -> Unit,
    onNavigateToMealDetail: (Meal) -> Unit,
    viewModel: DietDetailViewModel = koinViewModel { parametersOf(dietId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val diet = uiState.diet
    val backgroundColor = MaterialTheme.colorScheme.background

    Scaffold(
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            AddEntryFab(
                onClick = { viewModel.openSearchDialog() },
                text = "Add Meal"
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
                .background(backgroundColor)
        ) {
            // Calculate 30% of the screen height for the header
            val headerHeight = maxHeight * 0.3f

            // 1. FIXED HEADER SECTION (Image + Title)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
            ) {
                // Image taking up the whole header space
                if (!diet?.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = diet.imageUrl,
                        contentDescription = diet.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Smooth fade effect at the bottom of the image
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

                // Text content anchored to the bottom of the header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(horizontal = Spacing.m, vertical = Spacing.s)
                ) {
                    Text(
                        text = diet?.name ?: "",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // 2. SCROLLABLE CONTENT
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(
                    start = Spacing.m,
                    end = Spacing.m,
                    top = headerHeight, // Starts exactly where the header ends
                    bottom = Spacing.m
                ),
                horizontalArrangement = Arrangement.spacedBy(Spacing.m),
                verticalItemSpacing = Spacing.m
            ) {
                // Diet Description
                item(span = StaggeredGridItemSpan.FullLine) {
                    Text(
                        text = diet?.description ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.s)
                    )
                }

                // Related Meals
                if (uiState.relatedMeals.isNotEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Text(
                            text = "Meals",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = Spacing.xs)
                        )
                    }

                    items(uiState.relatedMeals, key = { it.meal.id }) { item ->
                        MealCard(
                            meal = item.meal,
                            calories = item.calories,
                            onClick = { onNavigateToMealDetail(item.meal) }
                        )
                    }
                }
            }

            // 3. FIXED BACK BUTTON
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
            title = "Log Meal",
            placeholder = "e.g., Chicken Salad",
            items = uiState.savedMeals,
            itemKey = { it.id },
            filter = { meal, query -> meal.name.contains(query, ignoreCase = true) },
            onDismiss = { viewModel.dismissSearchDialog() },
            itemContent = { meal ->
                MealItem(
                    meal = meal,
                    onAdd = { amount ->
                        viewModel.addMeal(mealId = meal.id, amount = amount)
                        viewModel.dismissSearchDialog()
                    }
                )
            }
        )
    }
}
