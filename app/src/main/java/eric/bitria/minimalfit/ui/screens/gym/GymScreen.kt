package eric.bitria.minimalfit.ui.screens.gym

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.components.shared.animations.SwipeToDeleteCard
import eric.bitria.minimalfit.ui.components.food.actions.PrimaryFloatingActionButton
import eric.bitria.minimalfit.ui.components.gym.cards.ExerciseCard
import eric.bitria.minimalfit.ui.components.gym.cards.GymSessionCard
import eric.bitria.minimalfit.ui.components.gym.cards.RoutineCard
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.gym.GymHomeViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymScreen(
    onNavigateToSession: (String?) -> Unit,
    onNavigateToExerciseProgression: (String) -> Unit,
    viewModel: GymHomeViewModel = koinViewModel()
) {
    val sessions by viewModel.recentSessions.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    // Exercise elements similar to FoodScreen diets
    val exerciseCardSize = screenHeight * 0.13f
    // Routine elements are bigger
    val routineCardSize = screenHeight * 0.2f

    var exerciseToDelete by remember { mutableStateOf<String?>(null) }

    if (exerciseToDelete != null) {
        AlertDialog(
            onDismissRequest = { exerciseToDelete = null },
            title = { Text("Delete Exercise") },
            text = { Text("Are you sure you want to delete this exercise? This will not delete past sets but the exercise won't appear in the catalog anymore.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteExercise(exerciseToDelete!!)
                        exerciseToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { exerciseToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    ScreenConfiguration(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Workout",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            PrimaryFloatingActionButton(
                onClick = { onNavigateToSession(null) },
                text = "Start Workout"
            )
        },
        bottomBar = true,
        quickActions = false
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = 120.dp // Space for FAB
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.l)
    ) {
        // 1. ROUTINES SECTION
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
                Text(
                    text = "Your Routines",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(start = Spacing.m, end = Spacing.m, top = Spacing.m)
                )

                val routinePagerState = rememberPagerState(pageCount = { 3 })
                HorizontalPager(
                    state = routinePagerState,
                    modifier = Modifier.fillMaxWidth(),
                    pageSize = PageSize.Fixed(routineCardSize),
                    contentPadding = PaddingValues(horizontal = Spacing.m),
                    pageSpacing = Spacing.m,
                    beyondViewportPageCount = 1
                ) { page ->
                    RoutineCard(
                        name = "Routine ${page + 1}",
                        exercisesCount = 4 + page,
                        onClick = { /* TODO: Navigate to routine detail */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.4f)
                    )
                }
            }
        }

        // 2. EXERCISES SECTION
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.m),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Exercises",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Exercise",
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable { /* TODO: Show add exercise dialog */ }
                            .padding(Spacing.xs),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                if (exercises.isNotEmpty()) {
                    val exercisePagerState = rememberPagerState(pageCount = { exercises.size })
                    HorizontalPager(
                        state = exercisePagerState,
                        modifier = Modifier.fillMaxWidth(),
                        pageSize = PageSize.Fixed(exerciseCardSize),
                        contentPadding = PaddingValues(horizontal = Spacing.m),
                        pageSpacing = Spacing.m,
                        beyondViewportPageCount = 1
                    ) { page ->
                        val exercise = exercises[page]
                        ExerciseCard(
                            exercise = exercise,
                            onClick = { onNavigateToExerciseProgression(exercise.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )
                    }
                } else {
                    Text(
                        text = "No exercises yet",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = Spacing.m),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3. WORKOUT HISTORY SECTION
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.m),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Workout History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }

        if (sessions.isNotEmpty()) {
            items(sessions, key = { it.id }) { session ->
                SwipeToDeleteCard(
                    onDismiss = { viewModel.deleteSession(session.id) },
                    modifier = Modifier
                        .padding(horizontal = Spacing.m)
                        .clip(MaterialTheme.shapes.extraLarge)
                ) {
                    GymSessionCard(
                        title = session.title,
                        duration = session.duration,
                        subtitle = session.subtitle,
                        exercisesCount = session.exercisesCount,
                        setsCount = session.setsCount,
                        volume = session.volume,
                        onClick = { onNavigateToSession(session.id) }
                    )
                }
            }
        } else {
            item {
                Text(
                    text = "No workouts yet. Start your first session!",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = Spacing.m),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}
