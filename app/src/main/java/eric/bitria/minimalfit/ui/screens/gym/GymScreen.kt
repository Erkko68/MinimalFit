package eric.bitria.minimalfit.ui.screens.gym

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.components.animations.SwipeToDeleteCard
import eric.bitria.minimalfit.ui.components.food.actions.PrimaryFloatingActionButton
import eric.bitria.minimalfit.ui.components.gym.cards.GymSessionCard
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

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Workouts", "Exercises")

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
                        text = "Gym Activities",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                PrimaryFloatingActionButton(
                    onClick = { onNavigateToSession(null) },
                    text = "Start Workout"
                )
            }
        },
        bottomBar = true,
        quickActions = true
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        PrimaryTabRow(selectedTabIndex = selectedTab, modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.m)) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        if (selectedTab == 0) {
            if (sessions.isNotEmpty()) {
                Text(
                    text = "Recent workouts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = Spacing.s)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Spacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    items(sessions, key = { it.id }) { session ->
                        SwipeToDeleteCard(
                            onDismiss = { viewModel.deleteSession(session.id) },
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.extraLarge)
                                .animateItem()
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
                }
            } else {
                Text(
                    text = "No workouts yet. Start your first session!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        } else {
            var newExerciseName by remember { mutableStateOf("") }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newExerciseName,
                    onValueChange = { newExerciseName = it },
                    label = { Text("New Exercise") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (newExerciseName.isNotBlank()) {
                            viewModel.addExercise(newExerciseName)
                            newExerciseName = ""
                        }
                    },
                    modifier = Modifier.padding(start = Spacing.m)
                ) {
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.padding(Spacing.s))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.m)
            ) {
                items(exercises, key = { it.id }) { exercise ->
                    SwipeToDeleteCard(
                        onDismiss = { exerciseToDelete = exercise.id },
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraLarge)
                            .animateItem()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            shape = MaterialTheme.shapes.extraLarge,
                            modifier = Modifier.fillMaxWidth().clickable { onNavigateToExerciseProgression(exercise.id) }
                        ) {
                            Text(
                                text = exercise.name,
                                modifier = Modifier.padding(Spacing.l),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
