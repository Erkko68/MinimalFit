package eric.bitria.minimalfit.ui.screens.gym

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.data.entity.gym.SessionStatus
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.components.food.dialogs.SearchableItemDialog
import eric.bitria.minimalfit.ui.components.requirements.permission.RequireNotificationPermission
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.gym.GymSessionViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymSessionScreen(
    sessionId: String?,
    onNavigateBack: () -> Unit,
    viewModel: GymSessionViewModel = koinViewModel { parametersOf(sessionId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val timerText by viewModel.timerText.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showFinishDialog by remember { mutableStateOf(false) }
    var showExerciseSearchDialog by remember { mutableStateOf(false) }
    var notificationPermissionGranted by remember { mutableStateOf(false) }
    val catalogExercises by viewModel.catalogExercises.collectAsState(initial = emptyList())

    if (!notificationPermissionGranted) {
        RequireNotificationPermission(onPermissionResult = { notificationPermissionGranted = it })
    }

    if (showFinishDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text(text = "Workout in progress") },
            text = { Text("Do you want to finish the workout, or keep it running in the background with a pop-up notification when you leave the app?") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showFinishDialog = false
                        viewModel.finishSession()
                        onNavigateBack()
                    }
                ) {
                    Text("Finish Workout")
                }
            },
            dismissButton = {
                Row {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            showFinishDialog = false
                            onNavigateBack()
                        }
                    ) {
                        Text("Keep Running")
                    }
                    androidx.compose.material3.TextButton(
                        onClick = { showFinishDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    if (showExerciseSearchDialog) {
        SearchableItemDialog(
            title = "Add Exercise",
            placeholder = "Search or create new...",
            items = catalogExercises,
            itemKey = { it.id },
            filter = { item, query -> item.name.contains(query, ignoreCase = true) },
            onDismiss = { showExerciseSearchDialog = false },
            onCreateNew = { newName ->
                viewModel.createNewExerciseAndAddSet(newName)
                showExerciseSearchDialog = false
            }
        ) { exercise ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.addSet(exercise.id)
                        showExerciseSearchDialog = false
                    }
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

    androidx.activity.compose.BackHandler(enabled = uiState.session?.status == SessionStatus.ACTIVE) {
        showFinishDialog = true
    }

    ScreenConfiguration(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Workout Session",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = timerText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.session?.status == SessionStatus.ACTIVE) {
                            showFinishDialog = true
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.session?.status == SessionStatus.ACTIVE) {
                        IconButton(onClick = {
                            showFinishDialog = true
                        }) {
                            Icon(Icons.Filled.Check, contentDescription = "Finish workout")
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = false,
        quickActions = false
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        val isActive = uiState.session?.status == SessionStatus.ACTIVE

        // Lista de ejercicios y sets
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            items(uiState.exercises, key = { it.exercise.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.m),
                        verticalArrangement = Arrangement.spacedBy(Spacing.s)
                    ) {
                        Text(
                            text = item.exercise.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        item.sets.forEachIndexed { index, set ->
                            var weightText by remember(set.id) { mutableStateOf(if (set.weight > 0) set.weight.toString() else "") }
                            var repsText by remember(set.id) { mutableStateOf(if (set.reps > 0) set.reps.toString() else "") }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Spacing.xs),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(24.dp)
                                )

                                OutlinedTextField(
                                    value = weightText,
                                    onValueChange = {
                                        weightText = it
                                        val weight = it.toFloatOrNull() ?: 0f
                                        viewModel.updateSet(set.copy(weight = weight))
                                    },
                                    label = { Text("kg") },
                                    enabled = isActive,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = repsText,
                                    onValueChange = {
                                        repsText = it
                                        val reps = it.toIntOrNull() ?: 0
                                        viewModel.updateSet(set.copy(reps = reps))
                                    },
                                    label = { Text("reps") },
                                    enabled = isActive,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                val isCompleted = set.isCompleted
                                IconButton(
                                    onClick = { 
                                        if (isActive) {
                                            viewModel.updateSet(set.copy(isCompleted = !isCompleted))
                                        }
                                    },
                                    modifier = Modifier.background(
                                        color = if (isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = MaterialTheme.shapes.small
                                    )
                                ) {
                                    Icon(
                                        Icons.Filled.Check, 
                                        contentDescription = "Complete set",
                                        tint = if (isCompleted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (isActive) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(onClick = { viewModel.addSet(item.exercise.id) }) {
                                    Text("+ Set")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isActive) {
            // Botón de terminar sesión, alineado al bottom con insets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = { showFinishDialog = true }) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Text("Finish workout", modifier = Modifier.padding(start = 8.dp))
                }

                Button(
                    onClick = { showExerciseSearchDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.xl)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Add Exercise")
                }
            }
        }
    }
}
