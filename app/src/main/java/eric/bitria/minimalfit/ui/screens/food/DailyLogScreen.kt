package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.ui.components.food.DailyCalorieCircleCard
import eric.bitria.minimalfit.ui.components.food.DailyMealsSection
import eric.bitria.minimalfit.ui.components.food.dialogs.MealSearchDialog
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.DailyCalorieData
import eric.bitria.minimalfit.ui.viewmodels.DailyLogViewModel
import eric.bitria.minimalfit.ui.viewmodels.FoodViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLogScreen(
    dayIndex: Int,
    onBackClick: () -> Unit,
    foodViewModel: FoodViewModel = koinViewModel(),
    dailyLogViewModel: DailyLogViewModel = koinViewModel()
) {
    val foodState by foodViewModel.uiState.collectAsState()
    val dailyLogState by dailyLogViewModel.buildUiState(dayIndex).collectAsState()

    val dailyData: DailyCalorieData? = foodState.weeklyProgress.getOrNull(dayIndex)

    if (dailyData != null) {
        val progress = if (dailyData.goalCalories > 0) {
            dailyData.currentCalories.toFloat() / dailyData.goalCalories
        } else 0f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.m)
        ) {
            // Back button + calorie circle in a row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = Spacing.m),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    DailyCalorieCircleCard(dailyData, progress)
                }
                // Mirrors button width to keep the circle visually centred
                Spacer(modifier = Modifier.size(48.dp))
            }

            DailyMealsSection(
                meals = dailyLogState.meals,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (dailyLogState.showSearchDialog) {
        MealSearchDialog(
            savedMeals = foodState.savedMeals,
            onDismiss = { dailyLogViewModel.dismissSearchDialog() },
            onAddMeal = { meal -> dailyLogViewModel.addMeal(dayIndex, meal) }
        )
    }
}