package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import eric.bitria.minimalfit.ui.components.food.DailyCalorieCircleCard
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.FoodViewModel
import eric.bitria.minimalfit.ui.viewmodels.Meal
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLogScreen(
    dayIndex: Int,
    onBackClick: () -> Unit,
    viewModel: FoodViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dailyData = uiState.weeklyProgress.getOrNull(dayIndex)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Daily Log", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (dailyData != null) {
            val progress = if (dailyData.goalCalories > 0) {
                dailyData.currentCalories.toFloat() / dailyData.goalCalories
            } else 0f

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Progress Indicator at the top
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(Spacing.m)
                ) {
                    DailyCalorieCircleCard(dailyData, progress)
                }

                Text(
                    text = "Add Meals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.02).em,
                    modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Spacing.m),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    items(uiState.savedMeals) { meal ->
                        MealAddCard(meal = meal, onAdd = { /* Logic to add meal to day */ })
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Day not found")
            }
        }
    }
}

@Composable
fun MealAddCard(meal: Meal, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .padding(Spacing.m)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${meal.calories} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledIconButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add meal")
            }
        }
    }
}