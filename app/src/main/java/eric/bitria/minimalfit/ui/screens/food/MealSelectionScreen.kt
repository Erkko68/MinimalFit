package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.ui.components.food.cards.DiaryMealRow
import eric.bitria.minimalfit.ui.components.food.cards.EmptyMealCard
import eric.bitria.minimalfit.ui.components.food.dialog.AddFoodDialog
import eric.bitria.minimalfit.ui.viewmodels.FoodViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.MealSelectionScreen(
    date: String,
    onBack: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    viewModel: FoodViewModel = koinViewModel()
) {
    val savedMeals by viewModel.savedMeals.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()
    val diaryMeals by viewModel.diaryMeals.collectAsState()
    val mealsForDate = diaryMeals[date] ?: emptyList()

    var showAddDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .sharedBounds(
                sharedContentState = rememberSharedContentState(key = "meal_container_$date"),
                animatedVisibilityScope = animatedVisibilityScope
            )
    ) {
        val width = maxWidth
        val height = maxHeight

        Column(modifier = Modifier.fillMaxSize()) {
            // Header box with date title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.25f)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(key = "meal_image_$date"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(width * 0.03f)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = date,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = width * 0.05f, bottom = height * 0.03f)
                )
            }

            // Diary content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = width * 0.05f, vertical = height * 0.02f)
            ) {
                Text(
                    text = "What did you eat?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = height * 0.015f)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(height * 0.015f),
                    modifier = Modifier.weight(1f)
                ) {
                    if (mealsForDate.isEmpty()) {
                        // Empty state card
                        item {
                            EmptyMealCard(onClick = { showAddDialog = true })
                        }
                    } else {
                        itemsIndexed(mealsForDate) { index, meal ->
                            DiaryMealRow(
                                meal = meal,
                                onRemove = { viewModel.removeMealFromDiary(date, index) }
                            )
                        }
                        // Add more button row
                        item {
                            OutlinedButton(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(percent = 35)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Add another meal")
                            }
                        }
                    }
                }
            }
        }
    }

    // Add food dialog
    if (showAddDialog) {
        AddFoodDialog(
            savedMeals = savedMeals,
            availableTags = availableTags,
            onDismiss = { showAddDialog = false },
            onMealSelected = { meal ->
                viewModel.addMealToDiary(date, meal)
                showAddDialog = false
            }
        )
    }
}
