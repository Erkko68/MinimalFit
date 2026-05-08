package eric.bitria.minimalfit.ui.screens.gym

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.components.food.dialogs.SearchableItemDialog
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.gym.SessionViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymSessionScreen(
    viewModel: SessionViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showFinishDialog by remember { mutableStateOf(false) }
    var showExerciseSearchDialog by remember { mutableStateOf(false) }

    fun formatDuration(duration: kotlin.time.Duration): String {
        val totalSeconds = duration.inWholeSeconds
        val mins = totalSeconds / 60
        val secs = totalSeconds % 60
        return "%02d:%02d".format(mins, secs)
    }

    BackHandler(enabled = uiState.isActive) {
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
                            text = formatDuration(uiState.elapsed),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.isActive) showFinishDialog = true else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            title = { Text(text = "Unfinished session") },
            text = { Text("You haven't finished your session yet. Do you want to finish it now?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFinishDialog = false
                        viewModel.finishSession()
                        onNavigateBack()
                    }
                ) { Text("Finish") }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(Spacing.m)) {
        // Summary card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.m, vertical = Spacing.s),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (!uiState.isActive) "Idle" else if (uiState.isPaused) "Paused" else "Active",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Sets: ${uiState.sets.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Rest timer
        if (uiState.isRestRunning) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.m),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Rest: ${formatDuration(uiState.restRemaining)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                        Button(onClick = { viewModel.startRest(30) }) { Text("+30s") }
                        Button(onClick = { viewModel.stopRest() }) { Text("Stop Rest") }
                    }
                }
            }
        }

        // Sets list
        LazyColumn(modifier = Modifier.fillMaxSize().weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            item { Text(text = "Sets", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) }

            if (uiState.sets.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) {
                        Column(modifier = Modifier.padding(Spacing.m)) {
                            Text("No sets yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (!uiState.isPaused) Button(onClick = { showExerciseSearchDialog = true }) { Text("Add Exercise") }
                        }
                    }
                }
            }

            items(uiState.sets, key = { it.id }) { set ->
                Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) {
                    Row(modifier = Modifier.fillMaxWidth().padding(Spacing.m), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        val exerciseName = uiState.catalogExercises.find { it.id == set.exerciseId }?.name ?: set.exerciseId
                        Text(text = exerciseName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(text = "${set.weight} kg • ${set.reps} reps", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Bottom floating toolbar
        Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = Spacing.m), horizontalArrangement = Arrangement.SpaceEvenly) {
            if (!uiState.isActive || uiState.isPaused) {
                Button(onClick = { viewModel.startSession() }) { Text(if (!uiState.isActive) "Start" else "Resume") }
            } else {
                Button(onClick = { viewModel.pauseSession() }) { Text("Pause") }
            }

            Button(onClick = { viewModel.finishSession() }, enabled = uiState.isActive) { Text("Stop") }

            Button(onClick = { if (!uiState.isPaused) showExerciseSearchDialog = true }, enabled = uiState.isActive && !uiState.isPaused) { Text("+ Exercise") }
        }
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
                viewModel.createNewExerciseAndAddSet(newName)
                showExerciseSearchDialog = false
            }
        ) { exercise ->
            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.addSet(exercise.id)
                    showExerciseSearchDialog = false
                }
                .padding(horizontal = Spacing.m, vertical = Spacing.l),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = exercise.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
