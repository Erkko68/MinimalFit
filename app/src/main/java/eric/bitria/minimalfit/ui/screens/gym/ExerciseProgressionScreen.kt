package eric.bitria.minimalfit.ui.screens.gym
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.gym.ExerciseProgressionViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseProgressionScreen(
    exerciseId: String,
    onNavigateBack: () -> Unit,
    viewModel: ExerciseProgressionViewModel = koinViewModel { parametersOf(exerciseId) }
) {
    val uiState by viewModel.uiState.collectAsState()

    ScreenConfiguration(
        topBar = {
            TopAppBar(
                title = { Text(uiState.exercise?.name ?: "Progresión") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = false
    )

    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.groupedHistory.isEmpty()) {
            Text("No hay historial todavía.", modifier = Modifier.padding(Spacing.m))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.m)
            ) {
                uiState.groupedHistory.forEach { (dateStr, sets) ->
                    item {
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = Spacing.m, bottom = Spacing.s)
                        )
                    }
                    items(sets) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.xs)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.m),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Set ${item.set.orderInSession}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${item.set.weight} kg  ×  ${item.set.reps} reps",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
