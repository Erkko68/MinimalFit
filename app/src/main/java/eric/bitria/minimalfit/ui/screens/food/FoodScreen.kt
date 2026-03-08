package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import eric.bitria.minimalfit.data.repository.FoodCatalogRepository
import eric.bitria.minimalfit.ui.components.food.cards.MealCard
import eric.bitria.minimalfit.ui.components.food.progress.DailyProgressPager
import eric.bitria.minimalfit.ui.components.food.lists.StaggeredGrid
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.util.WeekViewHelper
import eric.bitria.minimalfit.ui.viewmodels.FoodViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.time.LocalDate

@Composable
fun FoodScreen(
    onNavigateToDailyLog: (LocalDate) -> Unit,
    viewModel: FoodViewModel = koinViewModel(),
    weekViewHelper: WeekViewHelper = koinInject(),
    foodCatalog: FoodCatalogRepository = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val weekDates = weekViewHelper.last7Days()

    // Refresh data when screen becomes visible
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.m)
    ) {
        DailyProgressPager(
            uiState = uiState,
            dates = weekDates,
            onDayClick = onNavigateToDailyLog,
            modifier = Modifier.weight(0.42f)
        )

        Text(
            text = "Your Meals",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.02).em,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.s)
        )

        StaggeredGrid(
            meals = foodCatalog.getAllMeals(),
            modifier = Modifier.weight(1f),
            itemContent = { meal ->
                MealCard(meal = meal)
            }
        )
    }
}