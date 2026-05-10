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
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import eric.bitria.minimalfit.util.shortMonthDay
import eric.bitria.minimalfit.util.hourMinute
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.data.entity.gym.RoutineSummary
import eric.bitria.minimalfit.ui.components.shared.animations.SwipeToDeleteCard
import eric.bitria.minimalfit.ui.components.food.actions.PrimaryFloatingActionButton
import eric.bitria.minimalfit.ui.components.gym.cards.ExerciseCard
import eric.bitria.minimalfit.ui.components.gym.cards.GymSessionCard
import eric.bitria.minimalfit.ui.components.gym.cards.RoutineCard
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.gym.GymViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymScreen(
    onNavigateToSession: (String?) -> Unit,
    onNavigateToExerciseProgression: (String) -> Unit,
    viewModel: GymViewModel = koinViewModel()
) {
    val sessions by viewModel.pastSessions.collectAsState()
    val exercises by viewModel.userExercises.collectAsState()
    val routines by viewModel.routines.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showAddRoutineDialog by remember { mutableStateOf(false) }
    var routineToRename by remember { mutableStateOf<RoutineSummary?>(null) }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    // Routine elements are bigger
    val routineCardSize = screenHeight * 0.2f

    // Deletion confirmation dialog removed; exercises can be deleted with a swipe gesture.

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
                        contentDescription = "Add Routine",
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { showAddRoutineDialog = true }
                            .padding(Spacing.xs),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                if (routines.isEmpty()) {
                    RoutineCard(
                        name = "Create Routine",
                        exercisesCount = 0,
                        onClick = { showAddRoutineDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.8f)
                    )
                } else {
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
                                name = routine.name,
                                exercisesCount = routine.exerciseCount,
                                onClick = { },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.4f),
                                onMoreClick = { routineToRename = routine }
                            )
                        }
                    }
                }
            }
        }

        // Exercises title (full width)
        item(span = StaggeredGridItemSpan.FullLine) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
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
                        .clickable { showAddExerciseDialog = true }
                        .padding(Spacing.xs),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Exercises grid items
        if (exercises.isNotEmpty()) {
            items(items = exercises, key = { it.id }) { exercise ->
                SwipeToDeleteCard(
                    onDismiss = { /* no-op; we use onDeleteRequested to avoid auto-dismiss */ },
                    onDeleteRequested = { viewModel.deleteExercise(exercise.id) },
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraLarge)
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
            item(span = StaggeredGridItemSpan.FullLine) {
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
            // Render each session as a full-line item
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
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraLarge)
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

    if (showAddExerciseDialog) {
        AddExerciseDialog(
            onDismiss = { showAddExerciseDialog = false },
            onCreate = { name, muscleGroup, isBodyweight, restSeconds ->
                viewModel.addExercise(name, muscleGroup, isBodyweight, restSeconds)
                showAddExerciseDialog = false
            }
        )
    }

    if (showAddRoutineDialog) {
        AddRoutineDialog(
            exercises = exercises,
            onDismiss = { showAddRoutineDialog = false },
            onCreate = { name, exerciseIds ->
                viewModel.createRoutine(name, exerciseIds)
                showAddRoutineDialog = false
            }
        )
    }

    routineToRename?.let { routine ->
        RenameRoutineDialog(
            routine = routine,
            onDismiss = { routineToRename = null },
            onRename = { newName ->
                viewModel.renameRoutine(routine.id, newName)
                routineToRename = null
            }
        )
    }
}

@Composable
private fun AddExerciseDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, muscleGroup: String?, isBodyweight: Boolean, restSeconds: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var muscleGroup by remember { mutableStateOf("") }
    var isBodyweight by remember { mutableStateOf(false) }
    var restSecondsText by remember { mutableStateOf("120") }
    val restSeconds = restSecondsText.toIntOrNull() ?: 120

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create exercise") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = muscleGroup,
                    onValueChange = { muscleGroup = it },
                    label = { Text("Muscle group") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = restSecondsText,
                    onValueChange = { value ->
                        restSecondsText = value.filter { it.isDigit() }.take(4)
                    },
                    label = { Text("Rest seconds") },
                    singleLine = true
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    Checkbox(
                        checked = isBodyweight,
                        onCheckedChange = { isBodyweight = it }
                    )
                    Text("Bodyweight exercise")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onCreate(name, muscleGroup, isBodyweight, restSeconds) }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RenameRoutineDialog(
    routine: RoutineSummary,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var name by remember(routine.id) { mutableStateOf(routine.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename routine") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Routine name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onRename(name) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AddRoutineDialog(
    exercises: List<eric.bitria.minimalfit.data.entity.gym.Exercise>,
    onDismiss: () -> Unit,
    onCreate: (name: String, exerciseIds: List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedExerciseIds by remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create routine") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.m)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Routine name") },
                    singleLine = true
                )
                if (exercises.isEmpty()) {
                    Text(
                        text = "Create an exercise first, then add it to a routine.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    exercises.forEach { exercise ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedExerciseIds = if (selectedExerciseIds.contains(exercise.id)) {
                                        selectedExerciseIds - exercise.id
                                    } else {
                                        selectedExerciseIds + exercise.id
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedExerciseIds.contains(exercise.id),
                                onCheckedChange = { checked ->
                                    selectedExerciseIds = if (checked) {
                                        selectedExerciseIds + exercise.id
                                    } else {
                                        selectedExerciseIds - exercise.id
                                    }
                                }
                            )
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && selectedExerciseIds.isNotEmpty(),
                onClick = { onCreate(name, selectedExerciseIds.toList()) }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
