package eric.bitria.minimalfit.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// --- Mock Data ---
data class SavedMeal(val name: String, val calories: Int, val description: String)

val mockMeals = listOf(
    SavedMeal("Oatmeal & Berries", 350, "Rolled oats, almond milk, strawberries, and blueberries."),
    SavedMeal("Grilled Chicken Salad", 450, "Mixed greens, cherry tomatoes, cucumber, 150g chicken breast."),
    SavedMeal("Protein Shake", 200, "1 scoop whey protein, water, half a banana.")
)

val mockDates = listOf("Sun, 22 Feb", "Mon, 23 Feb", "Today, 24 Feb", "Wed, 25 Feb", "Thu, 26 Feb")

// --- Container to handle the Shared Transition ---
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FoodScreenContainer() {
    // Track WHICH date is selected, so we know which transition keys to use
    var activeRegistrationDate by remember { mutableStateOf<String?>(null) }

    SharedTransitionLayout {
        AnimatedContent(
            targetState = activeRegistrationDate,
            label = "container_transform_transition",
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            },
        ) { targetDate ->
            if (targetDate != null) {
                FullRegistrationScreen(
                    date = targetDate,
                    onBack = { activeRegistrationDate = null },
                    animatedVisibilityScope = this@AnimatedContent
                )
            } else {
                FoodScreen(
                    onRegisterClick = { clickedDate -> activeRegistrationDate = clickedDate },
                    animatedVisibilityScope = this@AnimatedContent
                )
            }
        }
    }
}

// --- Main Screen (List View) ---
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.FoodScreen(
    onRegisterClick: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val pagerState = rememberPagerState(initialPage = 2, pageCount = { mockDates.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 16.dp)
    ) {
        // 1. Horizontal Pager for the cards
        HorizontalPager(
            state = pagerState,
            // Shows a peek of the next/prev cards to indicate swipeability
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            RegisterMealCard(
                date = mockDates[page],
                onClick = { onRegisterClick(mockDates[page]) },
                animatedVisibilityScope = animatedVisibilityScope
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Your Meals",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(mockMeals) { meal ->
                SavedMealCard(meal)
            }
        }
    }
}

// --- Top Registration Card (Source of Transition) ---
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.RegisterMealCard(
    date: String,
    onClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            // 2. Unique keys per date so the animation knows which card is expanding
            .sharedBounds(
                sharedContentState = rememberSharedContentState(key = "meal_container_$date"),
                animatedVisibilityScope = animatedVisibilityScope
            ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(key = "meal_image_$date"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fastfood,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(64.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap to track nutrition",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Meal",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// --- Full Screen Detail View (Destination of Transition) ---
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.FullRegistrationScreen(
    date: String,
    onBack: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .sharedBounds(
                sharedContentState = rememberSharedContentState(key = "meal_container_$date"),
                animatedVisibilityScope = animatedVisibilityScope
            ),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            // 3. Header taking over top area. Taller height.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // Taller to look like a true header
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(key = "meal_image_$date"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                // Apply statusBarsPadding ONLY to the button, so the background draws under the clock
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
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
                    .padding(24.dp)
            ) {
                Text(
                    text = "Log Meal for $date",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Meal Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Total Calories") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save to Diary")
                }
            }
        }
    }
}

// --- Bottom List Item Card ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedMealCard(meal: SavedMeal) {
    var isExpanded by remember { mutableStateOf(false) }

    OutlinedCard(
        onClick = { isExpanded = !isExpanded },
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${meal.calories} kcal",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Details",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = meal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}