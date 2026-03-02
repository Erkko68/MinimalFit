package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import eric.bitria.minimalfit.ui.components.food.search.FoodSearchContent
import eric.bitria.minimalfit.ui.viewmodels.FoodViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchScreen(
    onBack: () -> Unit,
    viewModel: FoodViewModel = koinViewModel()
) {
    val savedMeals by viewModel.savedMeals.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = maxWidth
        val height = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = width * 0.01f,
                        top = height * 0.005f,
                        bottom = height * 0.005f
                    )
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = "Search Meals",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = width * 0.01f)
                )
            }

            FoodSearchContent(
                savedMeals = savedMeals,
                availableTags = availableTags,
                autoFocus = true,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
