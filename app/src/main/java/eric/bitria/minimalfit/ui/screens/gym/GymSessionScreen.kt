package eric.bitria.minimalfit.ui.screens.gym

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.components.food.dialogs.SearchableItemDialog
import eric.bitria.minimalfit.ui.components.gym.SessionState
import eric.bitria.minimalfit.ui.components.gym.SessionToolbar
import eric.bitria.minimalfit.ui.components.gym.cards.SessionExerciseCard
import eric.bitria.minimalfit.ui.components.gym.dialogs.TimerDialog
import eric.bitria.minimalfit.ui.components.requirements.permission.RequireNotificationPermission
import eric.bitria.minimalfit.ui.components.shared.animations.SwipeToDeleteCard
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.gym.SessionViewModel
import eric.bitria.minimalfit.util.hourMinute
import eric.bitria.minimalfit.util.shortMonthDay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymSessionScreen(
    sessionId: String? = null,
    routineId: String? = null,
    replaceActiveWorkout: Boolean = false,
    viewModel: SessionViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var notificationPermissionGranted by remember { mutableStateOf(false) }

    LaunchedEffect(sessionId, routineId, replaceActiveWorkout) {
        viewModel.initialize(
            sessionId = sessionId,
            routineId = routineId,
            replaceActiveWorkout = replaceActiveWorkout
        )
    }

    var showFinishDialog by remember { mutableStateOf(false) }
    var showExerciseSearchDialog by remember { mutableStateOf(false) }
    var showRestDialog by remember { mutableStateOf(false) }
    var showSaveAsRoutineDialog by remember { mutableStateOf(false) }
    var editedTitle by remember(uiState.sessionTitle) { mutableStateOf(uiState.sessionTitle) }
    var collapsedExercises by remember { mutableStateOf(setOf<String>()) }

    fun formatDuration(duration: kotlin.time.Duration): String {
        val totalSeconds = duration.inWholeSeconds
        val mins = totalSeconds / 60
        val secs = totalSeconds % 60
        return "%02d:%02d".format(mins, secs)
    }

    val defaultTitle = uiState.sessionStartTime?.let { start ->
        val local = start.toLocalDateTime(TimeZone.currentSystemDefault())
        "${local.date.shortMonthDay()} • ${local.time.hourMinute()}"
    } ?: "Workout"

    val toolbarState = when {
        !uiState.isActive -> SessionState.IDLE
        uiState.isPaused && sessionId != null -> SessionState.VIEWING
        uiState.isPaused -> SessionState.PAUSED
        else -> SessionState.RUNNING
    }

    val interceptBack = toolbarState == SessionState.RUNNING || toolbarState == SessionState.PAUSED

    BackHandler(enabled = interceptBack) {
        showFinishDialog = true
    }

    if (!notificationPermissionGranted) {
        RequireNotificationPermission(onPermissionResult = { isGranted ->
            if (isGranted) {
                notificationPermissionGranted = true
            } else {
                onNavigateBack()
            }
        })
        return
    }

    ScreenConfiguration(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        BasicTextField(
                            value = editedTitle,
                            onValueChange = {
                                editedTitle = it
                                viewModel.updateSessionTitle(it)
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground,
                                letterSpacing = (-0.02).em
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                            decorationBox = { innerTextField ->
                                if (editedTitle.isBlank()) {
                                    Text(
                                        text = defaultTitle,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (uiState.isActive) {
                            Text(
                                text = formatDuration(uiState.elapsed),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (uiState.isPaused)
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (interceptBack) showFinishDialog = true else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isActive && uiState.exerciseGroups.isNotEmpty()) {
                        IconButton(onClick = { showSaveAsRoutineDialog = true }) {
                            Icon(Icons.Filled.Bookmark, contentDescription = "Save as routine")
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = false,
        quickActions = false
    )

    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Finish session?") },
            text = { Text("Do you want to finish and save this session?") },
            confirmButton = {
                TextButton(onClick = {
                    showFinishDialog = false
                    viewModel.finishSession()
                    onNavigateBack()
                }) { Text("Finish") }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showSaveAsRoutineDialog) {
        AlertDialog(
            onDismissRequest = { showSaveAsRoutineDialog = false },
            title = { Text("Save as routine?") },
            text = { Text("Save the current exercises as a new routine?") },
            confirmButton = {
                TextButton(onClick = {
                    showSaveAsRoutineDialog = false
                    viewModel.saveSessionAsRoutine()
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveAsRoutineDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showRestDialog) {
        TimerDialog(
            isRestRunning = uiState.isRestRunning,
            restRemaining = uiState.restRemaining,
            onDismiss = { showRestDialog = false },
            onStartRest = { seconds ->
                viewModel.startRest(seconds)
            },
            onAddTime = { seconds ->
                viewModel.startRest(seconds)
            },
            onStopRest = {
                viewModel.stopRest()
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val canEdit = uiState.isActive && !uiState.isPaused

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.m),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            item { Spacer(modifier = Modifier.height(Spacing.xs)) }

            if (uiState.isRestRunning) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRestDialog = true },
                        shape = MaterialTheme.shapes.large
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.m, vertical = Spacing.s),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Timer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatDuration(uiState.restRemaining),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (uiState.exerciseGroups.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.l),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (uiState.isActive) "No exercises yet"
                                       else "Press Start to begin your workout",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            items(items = uiState.exerciseGroups, key = { it.sessionExerciseId }) { group ->
                val isCollapsed = collapsedExercises.contains(group.sessionExerciseId)
                val card: @Composable () -> Unit = {
                    SessionExerciseCard(
                        exerciseName = group.exerciseName,
                        sets = group.sets,
                        isCollapsed = isCollapsed,
                        canEdit = canEdit,
                        createdAt = group.createdAt,
                        onToggleCollapse = {
                            collapsedExercises = if (isCollapsed)
                                collapsedExercises - group.sessionExerciseId
                            else
                                collapsedExercises + group.sessionExerciseId
                        },
                        onUpdateSet = { viewModel.updateSet(it) },
                        onDeleteSet = { viewModel.deleteSet(it) },
                        onAddSet = { w, r -> viewModel.addSet(group.sessionExerciseId, w, r) }
                    )
                }
                if (canEdit) {
                    SwipeToDeleteCard(
                        onDismiss = {},
                        onDeleteRequested = { viewModel.deleteExercise(group.sessionExerciseId) },
                        modifier = Modifier.clip(MaterialTheme.shapes.extraLarge)
                    ) { card() }
                } else {
                    card()
                }
            }

            item { Spacer(modifier = Modifier.height(Spacing.xxl)) }
        }

        SessionToolbar(
            state = toolbarState,
            onStart = {
                viewModel.startSession()
            },
            onPause = { viewModel.pauseSession() },
            onResume = {
                viewModel.resumeSession()
            },
            onStop = { showFinishDialog = true },
            onAddExercise = { showExerciseSearchDialog = true },
            onStartRest = { showRestDialog = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = Spacing.l)
        )
    }

    if (showExerciseSearchDialog) {
        SearchableItemDialog(
            title = "Add Exercise",
            placeholder = "Search...",
            items = uiState.catalogExercises,
            itemKey = { it.id },
            filter = { item, query -> item.name.contains(query, ignoreCase = true) },
            onDismiss = { showExerciseSearchDialog = false },
            onCreateNew = { newName ->
                viewModel.createNewExerciseAndAdd(newName)
            }
        ) { exercise ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.addExercise(exercise.id) }
                    .padding(horizontal = Spacing.m, vertical = Spacing.l),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
