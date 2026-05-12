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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.components.food.actions.PrimaryFloatingActionButton
import eric.bitria.minimalfit.ui.components.gym.cards.ExerciseCard
import eric.bitria.minimalfit.ui.components.gym.cards.GymSessionCard
import eric.bitria.minimalfit.ui.components.gym.cards.RoutineCard
import eric.bitria.minimalfit.ui.components.shared.animations.SwipeToDeleteCard
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.gym.GymViewModel
import eric.bitria.minimalfit.util.hourMinute
import eric.bitria.minimalfit.util.shortMonthDay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymScreen(
    onNavigateToSession: (String?) -> Unit,
    onNavigateToExerciseProgression: (String) -> Unit,
    onNavigateToRoutine: (String?) -> Unit,
    viewModel: GymViewModel = koinViewModel()
) {
    val sessions by viewModel.pastSessions.collectAsState()
    val exercises by viewModel.userExercises.collectAsState()
    val routines by viewModel.routines.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val routineCardSize = screenHeight * 0.2f
    val exerciseCardSize = screenHeight * 0.15f

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

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        verticalItemSpacing = Spacing.m,
        contentPadding = PaddingValues(
            bottom = Spacing.m,
            start = Spacing.m,
            end = Spacing.m
        )
    ) {
        // Routines carousel (full width)
        item(span = StaggeredGridItemSpan.FullLine) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Routines",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Routine",
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable { onNavigateToRoutine(null) }
                            .padding(Spacing.xs),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                if (routines.isNotEmpty()) {
                    val routinePagerState = rememberPagerState(pageCount = { routines.size })
                    HorizontalPager(
                        state = routinePagerState,
                        modifier = Modifier.fillMaxWidth(),
                        pageSize = PageSize.Fixed(routineCardSize),
                        pageSpacing = Spacing.m,
                        beyondViewportPageCount = 1
                    ) { page ->
                        val routine = routines[page]
                        SwipeToDeleteCard(
                            onDismiss = {},
                            onDeleteRequested = { viewModel.deleteRoutine(routine.id) },
                            modifier = Modifier.clip(MaterialTheme.shapes.extraLarge)
                        ) {
                            RoutineCard(
                                routine = routine,
                                exercisesCount = 0,
                                onClick = { onNavigateToRoutine(routine.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.4f)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No routines yet. Create your first one!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Exercises title (full width)
        item(span = StaggeredGridItemSpan.FullLine) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
        }

        // Exercises pager (full width)
        item(span = StaggeredGridItemSpan.FullLine) {
            if (exercises.isNotEmpty()) {
                val exercisePagerState = rememberPagerState(pageCount = { exercises.size })
                HorizontalPager(
                    state = exercisePagerState,
                    modifier = Modifier.fillMaxWidth(),
                    pageSize = PageSize.Fixed(exerciseCardSize),
                    pageSpacing = Spacing.m,
                    beyondViewportPageCount = 1
                ) { page ->
                    val exercise = exercises[page]
                    SwipeToDeleteCard(
                        onDismiss = {},
                        onDeleteRequested = { viewModel.deleteExercise(exercise.id) },
                        modifier = Modifier.clip(MaterialTheme.shapes.extraLarge)
                    ) {
                        ExerciseCard(
                            exercise = exercise,
                            onClick = { onNavigateToExerciseProgression(exercise.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.4f)
                        )
                    }
                }
            } else {
                Text(
                    text = "No exercises yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Workout history title
        item(span = StaggeredGridItemSpan.FullLine) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
            sessions.forEach { session ->
                item(span = StaggeredGridItemSpan.FullLine) {
                    val sess = session.session
                    val setsList = session.sets
                    val localStart = sess.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
                    val dateString = "${localStart.date.shortMonthDay()} • ${localStart.time.hourMinute()}"
                    val title = sess.title.ifBlank { "Workout" }
                    val mins = sess.durationSeconds / 60
                    val secs = sess.durationSeconds % 60
                    val duration = "%d:%02d".format(mins, secs)
                    val exercisesCount = setsList.map { it.sessionExerciseId }.distinct().size
                    val volume = setsList.fold(0f) { acc, s -> acc + (s.weight * s.reps) }

                    SwipeToDeleteCard(
                        onDismiss = { viewModel.deleteSession(sess.id) },
                        modifier = Modifier.clip(MaterialTheme.shapes.extraLarge)
                    ) {
                        GymSessionCard(
                            dateString = dateString,
                            title = title,
                            duration = duration,
                            exercisesCount = exercisesCount,
                            setsCount = setsList.size,
                            volume = volume,
                            onClick = { onNavigateToSession(sess.id) }
                        )
                    }
                }
            }
        } else {
            item(span = StaggeredGridItemSpan.FullLine) {
                Text(
                    text = "No workouts yet. Start your first session!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}
