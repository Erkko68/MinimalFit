package eric.bitria.minimalfit.ui.components.food

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    val mealsByTag by remember(viewModel) { mutableStateOf(viewModel.getMealsByTag()) }

    BoxWithConstraints(modifier = modifier
        .fillMaxSize()
        .sharedBounds(
            sharedContentState = rememberSharedContentState(key = "meal_container_$date"),
            animatedVisibilityScope = animatedVisibilityScope
        )
    ) {
        val width = maxWidth
        val height = maxHeight

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.3f)
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
                        .padding(width * 0.04f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(width * 0.05f)
            ) {
                Text(
                    text = "What did you eat today?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = height * 0.02f)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(height * 0.02f)) {
                    mealsByTag.forEach { (tag, meals) ->
                        item {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = height * 0.01f)
                            )
                        }
                        items(meals) { meal ->
                            MealSelectionItem(meal)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Add to Diary")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealSelectionItem(meal: eric.bitria.minimalfit.ui.viewmodels.SavedMeal) {
    var isChecked by remember { mutableStateOf(false) }
    
    ListItem(
        headlineContent = { Text(meal.name, fontWeight = FontWeight.Medium) },
        supportingContent = { Text("${meal.calories} kcal") },
        trailingContent = {
            Checkbox(checked = isChecked, onCheckedChange = { isChecked = it })
        }
    )
}
