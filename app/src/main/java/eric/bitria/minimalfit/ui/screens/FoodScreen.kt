package eric.bitria.minimalfit.ui.screens

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
import eric.bitria.minimalfit.ui.components.food.RegisterMealCard
import eric.bitria.minimalfit.ui.components.food.SavedMealCard
import eric.bitria.minimalfit.ui.viewmodels.FoodViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.FoodScreen(
    animatedVisibilityScope: AnimatedVisibilityScope,
    onRegisterClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: FoodViewModel = koinViewModel()
) {
    val dates by viewModel.mockDates.collectAsState()
    val filteredMeals by viewModel.filteredMeals.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = if (dates.isNotEmpty()) dates.size - 1 else 0,
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
                RegisterMealCard(
                    date = dates[page],
                    onClick = { onRegisterClick(dates[page]) },
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
                items(filteredMeals) { meal ->
                    SavedMealCard(meal)
                }
            }
        }
    }
}


