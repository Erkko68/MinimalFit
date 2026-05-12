package eric.bitria.minimalfit.ui.screens.gym

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import eric.bitria.minimalfit.ui.components.gym.cards.RoutineExerciseCard
import eric.bitria.minimalfit.ui.components.shared.animations.SwipeToDeleteCard
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.gym.RoutineViewModel
import org.koin.androidx.compose.koinViewModel

private val DAY_LABELS = listOf("M", "T", "W", "T", "F", "S", "S")
private val DAY_NUMBERS = listOf(1, 2, 3, 4, 5, 6, 7)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoutineDetailScreen(
    routineId: String? = null,
    viewModel: RoutineViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showExerciseSearchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(routineId) {
        viewModel.initialize(routineId)
    }

    var editedName by remember(uiState.routine?.name) {
        mutableStateOf(uiState.routine?.name ?: "")
    }
    var editedDescription by remember(uiState.routine?.description) {
        mutableStateOf(uiState.routine?.description ?: "")
    }

    ScreenConfiguration(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    BasicTextField(
                        value = editedName,
                        onValueChange = {
                            editedName = it
                            viewModel.updateName(it)
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = (-0.02).em
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                        decorationBox = { innerTextField ->
                            if (editedName.isBlank()) {
                                Text(
                                    text = "Routine name",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = false,
        quickActions = false
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.m),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            item { Spacer(modifier = Modifier.height(Spacing.xs)) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.m),
                        verticalArrangement = Arrangement.spacedBy(Spacing.m)
                    ) {
                        BasicTextField(
                            value = editedDescription,
                            onValueChange = {
                                editedDescription = it
                                viewModel.updateDescription(it)
                            },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                            decorationBox = { innerTextField ->
                                if (editedDescription.isBlank()) {
                                    Text(
                                        text = "Description (optional)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        val selectedDays = uiState.routine?.daysOfWeek ?: emptySet()
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            DAY_NUMBERS.forEachIndexed { index, day ->
                                FilterChip(
                                    selected = selectedDays.contains(day),
                                    onClick = { viewModel.toggleDay(day) },
                                    label = {
                                        Text(
                                            text = DAY_LABELS[index],
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                )
                            }
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
                                text = "No exercises yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            items(items = uiState.exerciseGroups, key = { it.routineExerciseId }) { group ->
                SwipeToDeleteCard(
                    onDismiss = {},
                    onDeleteRequested = { viewModel.deleteExercise(group.routineExerciseId) },
                    modifier = Modifier.clip(MaterialTheme.shapes.extraLarge)
                ) {
                    RoutineExerciseCard(
                        exerciseName = group.exerciseName,
                        sets = group.sets,
                        onUpdateSet = { viewModel.updateSet(it) },
                        onDeleteSet = { viewModel.deleteSet(it) },
                        onAddSet = { viewModel.addSet(group.routineExerciseId) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(Spacing.xxl)) }
        }

        FloatingActionButton(
            onClick = { showExerciseSearchDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = Spacing.l, end = Spacing.m)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Exercise")
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
                viewModel.createNewExerciseAndAdd(newName)
            }
        ) { exercise ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.addExercise(exercise.id)
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
}
