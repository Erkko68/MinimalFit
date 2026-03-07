package eric.bitria.minimalfit.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.min
import coil.compose.AsyncImage
import eric.bitria.minimalfit.ui.viewmodels.DailyCalorieData
import eric.bitria.minimalfit.ui.viewmodels.FoodUiState
import eric.bitria.minimalfit.ui.viewmodels.FoodViewModel
import eric.bitria.minimalfit.ui.viewmodels.Meal
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodScreen(
    viewModel: FoodViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            val horizontalPadding = maxWidth * 0.04f
            val verticalPadding = maxHeight * 0.02f

            Column(modifier = Modifier.fillMaxSize()) {
                DailyProgressPager(
                    uiState = uiState,
                    modifier = Modifier.weight(0.42f)
                )

                Text(
                    text = "Your Meals",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = horizontalPadding,
                            vertical = verticalPadding
                        )
                )

                MealsStaggeredGrid(
                    meals = uiState.savedMeals,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DailyProgressPager(uiState: FoodUiState, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(pageCount = { uiState.weeklyProgress.size })

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val totalWidth = maxWidth

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f),
                contentPadding = PaddingValues(horizontal = totalWidth * 0.15f),
                pageSpacing = totalWidth * 0.05f
            ) { pageIndex ->
                val dailyData = uiState.weeklyProgress[pageIndex]
                val progress = if (dailyData.goalCalories > 0) {
                    dailyData.currentCalories.toFloat() / dailyData.goalCalories
                } else 0f

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(totalWidth * 0.04f)
                ) {
                    DailyCalorieCircleCard(dailyData, progress)
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(0.1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(uiState.weeklyProgress.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

                    val indicatorWidth = if (isSelected) totalWidth * 0.06f else totalWidth * 0.02f

                    Surface(
                        modifier = Modifier
                            .padding(horizontal = totalWidth * 0.01f)
                            .height(totalWidth * 0.02f)
                            .width(indicatorWidth),
                        shape = CircleShape,
                        color = color
                    ) {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DailyCalorieCircleCard(dailyData: DailyCalorieData, progress: Float) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val cardSize = min(maxWidth, maxHeight)
            val indicatorSize = cardSize * 0.95f

            val strokeWidthDp = cardSize * 0.05f
            val strokeWidthPx = with(LocalDensity.current) { strokeWidthDp.toPx() }
            val waveLengthDp = cardSize * 0.25f

            CircularWavyProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.size(indicatorSize),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                stroke = Stroke(width = strokeWidthPx),
                trackStroke = Stroke(width = strokeWidthPx),
                wavelength = waveLengthDp
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = dailyData.dayName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.05.em
                )
                Text(
                    text = dailyData.dayNumber.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(cardSize * 0.02f))
                Text(
                    text = "${dailyData.currentCalories}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "kcal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MealsStaggeredGrid(meals: List<Meal>, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val spacing = maxWidth * 0.04f
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(spacing),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalItemSpacing = spacing
        ) {
            itemsIndexed(meals) { index, meal ->
                MealCard(meal = meal)
            }
        }
    }
}

@Composable
fun MealCard(meal: Meal) {
    val hasImage = !meal.imageUrl.isNullOrEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20),
        colors = CardDefaults.cardColors(
            containerColor = if (hasImage)
                MaterialTheme.colorScheme.surfaceContainerHigh
            else
                MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        ) {
            if (hasImage) {
                AsyncImage(
                    model = meal.imageUrl,
                    contentDescription = meal.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (hasImage) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 1.1.em,
                    letterSpacing = (-0.02).em
                )

                Surface(
                    color = if (hasImage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                ) {
                    Text(
                        text = "${meal.calories} kcal",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = if (hasImage) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}