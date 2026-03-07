package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import eric.bitria.minimalfit.ui.components.food.DailyProgressPager
import eric.bitria.minimalfit.ui.components.food.MealsStaggeredGrid
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.FoodViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun FoodScreen(
    viewModel: FoodViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        DailyProgressPager(
            uiState = uiState,
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
                .padding(horizontal = Spacing.m, vertical = Spacing.s)
        )

        MealsStaggeredGrid(
            meals = uiState.savedMeals,
            modifier = Modifier.weight(1f)
        )
    }
}
