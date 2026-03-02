package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.ui.components.food.cards.RegisterMealCard
import eric.bitria.minimalfit.ui.components.food.cards.SavedMealCard
import eric.bitria.minimalfit.ui.viewmodels.FoodViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.FoodScreen(
    animatedVisibilityScope: AnimatedVisibilityScope,
    onRegisterClick: (LocalDate) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: FoodViewModel = koinViewModel()
) {
    val savedMeals by viewModel.savedMeals.collectAsState()
    val dates = remember { viewModel.recentDays() }

    val pagerState = rememberPagerState(
        initialPage = dates.size - 1,
        pageCount = { dates.size }
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = maxWidth
        val height = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = height * 0.02f)
        ) {
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = width * 0.1f),
                pageSpacing = width * 0.04f,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.25f)
            ) { page ->
                val date = dates[page]
                RegisterMealCard(
                    date = date,
                    onClick = { onRegisterClick(date) },
                    animatedVisibilityScope = animatedVisibilityScope,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Section header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = width * 0.05f,
                        end = width * 0.03f,
                        top = height * 0.02f,
                        bottom = height * 0.008f
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Saved Meals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search meals"
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(height * 0.015f),
                contentPadding = PaddingValues(
                    horizontal = width * 0.05f,
                    vertical = height * 0.01f
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                items(savedMeals) { meal ->
                    SavedMealCard(meal)
                }
            }
        }
    }
}
