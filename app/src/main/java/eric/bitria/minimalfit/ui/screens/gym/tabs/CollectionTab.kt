package eric.bitria.minimalfit.ui.screens.gym.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.ui.components.gym.cards.RoutineListItem
import eric.bitria.minimalfit.ui.components.gym.dialogs.CreateExerciseDialog
import eric.bitria.minimalfit.ui.components.gym.rows.ExerciseListItem
import eric.bitria.minimalfit.ui.components.shared.animations.SwipeToDeleteCard
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.gym.GymCollectionViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CollectionTab(
    onNavigateToRoutine: (String?) -> Unit,
    onNavigateToExerciseProgression: (String) -> Unit,
    viewModel: GymCollectionViewModel = koinViewModel()
) {
    val routinesWithDetails by viewModel.routinesWithDetails.collectAsState()
    val exercises by viewModel.userExercises.collectAsState()
    var showAddExerciseDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        item { Spacer(modifier = Modifier.height(Spacing.xs)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Routines",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
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
        }

        if (routinesWithDetails.isEmpty()) {
            item {
                Text(
                    text = "No routines yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(routinesWithDetails, key = { it.routine.id }) { item ->
                SwipeToDeleteCard(
                    onDismiss = {},
                    onDeleteRequested = { viewModel.deleteRoutine(item.routine.id) },
                    modifier = Modifier.clip(MaterialTheme.shapes.large)
                ) {
                    RoutineListItem(
                        routine = item.routine,
                        exerciseCount = item.exerciseCount,
                        setCount = item.setCount,
                        onClick = { onNavigateToRoutine(item.routine.id) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(Spacing.m)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Exercises",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
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

        if (exercises.isEmpty()) {
            item {
                Text(
                    text = "No exercises yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            itemsIndexed(exercises, key = { _, e -> e.id }) { index, exercise ->
                Column {
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = Spacing.s),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                    SwipeToDeleteCard(
                        onDismiss = {},
                        onDeleteRequested = { viewModel.deleteExercise(exercise.id) },
                        modifier = Modifier.clip(MaterialTheme.shapes.medium)
                    ) {
                        ExerciseListItem(
                            exercise = exercise,
                            onClick = { onNavigateToExerciseProgression(exercise.id) }
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(Spacing.xxl)) }
    }

    if (showAddExerciseDialog) {
        CreateExerciseDialog(
            onDismiss = { showAddExerciseDialog = false },
            onCreate = { name, muscleGroup, isBodyweight, restSeconds ->
                viewModel.addExercise(name, muscleGroup, isBodyweight, restSeconds)
                showAddExerciseDialog = false
            }
        )
    }
}

