package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import eric.bitria.minimalfit.data.entity.food.Meal
import eric.bitria.minimalfit.ui.components.animations.SwipeToDeleteCard
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
            val headerHeight = maxHeight * 0.3f
            var headerAreaHeight by remember { mutableIntStateOf(0) }

            // 1. SCROLLABLE CONTENT (Rendered first to be below the header)
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(
                    start = Spacing.m,
                    end = Spacing.m,
                    top = with(LocalDensity.current) { headerAreaHeight.toDp() },
                    bottom = Spacing.m
                ),
                horizontalArrangement = Arrangement.spacedBy(Spacing.m),
                verticalItemSpacing = Spacing.m
            ) {
                // Related Meals
                if (uiState.relatedMeals.isNotEmpty()) {
                    items(uiState.relatedMeals, key = { it.meal.id }) { item ->
                        SwipeToDeleteCard(
                            onDismiss = { viewModel.removeMealFromDiet(item.meal.id) },
                            modifier = Modifier.clip(MaterialTheme.shapes.extraLarge)
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

            // 2. FIXED HEADER SECTION (Rendered second to be on top)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { headerAreaHeight = it.height }
                    .background(backgroundColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(headerHeight)
                ) {
                    if (diet != null && !diet.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = diet.imageUrl,
                            contentDescription = diet.name,
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
                            text = diet?.name ?: "",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (diet != null && diet.description.isNotEmpty()) {
                            Text(
                                text = diet.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(Spacing.m))
                        Text(
                            text = "Meals",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
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
                    onAdd = { amount, portionMode ->
                        viewModel.addMeal(mealId = meal.id, amount = amount, portionMode = portionMode)
                        viewModel.dismissSearchDialog()
                    }
                )
            }
        )
    }
}
