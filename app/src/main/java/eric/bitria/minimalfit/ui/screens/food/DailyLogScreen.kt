package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import eric.bitria.minimalfit.ui.components.food.AddEntryFab
import eric.bitria.minimalfit.ui.components.food.DailyCalorieCircleCard
import eric.bitria.minimalfit.ui.components.food.EmptyMealsPlaceholder
import eric.bitria.minimalfit.ui.components.food.MealsStaggeredGrid
import eric.bitria.minimalfit.ui.components.food.dialogs.MealSearchDialog
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.DailyCalorieData
import eric.bitria.minimalfit.ui.viewmodels.DailyLogViewModel
import eric.bitria.minimalfit.ui.viewmodels.FoodViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DailyLogScreen(
    dayIndex: Int,
    openSearch: Boolean = false,
    onBackClick: () -> Unit,
    foodViewModel: FoodViewModel = koinViewModel(),
    dailyLogViewModel: DailyLogViewModel = koinViewModel()
) {
    val foodState by foodViewModel.uiState.collectAsState()
    val dailyLogState by dailyLogViewModel.buildUiState(dayIndex).collectAsState()

    LaunchedEffect(openSearch) {
        if (openSearch) {
            dailyLogViewModel.openSearchDialog()
        }
    }

    val dailyData: DailyCalorieData? = foodState.weeklyProgress.getOrNull(dayIndex)

    if (dailyData != null) {
        val progress = if (dailyData.goalCalories > 0) {
            dailyData.currentCalories.toFloat() / dailyData.goalCalories
        } else 0f

        Scaffold(
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                AddEntryFab(onClick = { dailyLogViewModel.openSearchDialog() })
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .padding(horizontal = Spacing.m)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.s, bottom = Spacing.m)
                ) {
                    // Back button pinned to the top left
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }

                    // Progress Widget pinned to the top center
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .aspectRatio(1f)
                            .align(Alignment.TopCenter)
                    ) {
                        DailyCalorieCircleCard(dailyData, progress)
                    }
                }

                Text(
                    text = "Meals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.02).em,
                    modifier = Modifier.padding(bottom = Spacing.s)
                )

                if (dailyLogState.meals.isEmpty()) {
                    EmptyMealsPlaceholder()
                } else {
                    MealsStaggeredGrid(
                        meals = dailyLogState.meals,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    if (dailyLogState.showSearchDialog) {
        MealSearchDialog(
            savedMeals = dailyLogState.savedMeals,
            onDismiss = { dailyLogViewModel.dismissSearchDialog() },
            onAddMeal = { meal -> dailyLogViewModel.addMeal(dayIndex, meal) }
        )
    }
}