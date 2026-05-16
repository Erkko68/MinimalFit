package eric.bitria.minimalfit.ui.screens.gym

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.components.food.actions.PrimaryFloatingActionButton
import eric.bitria.minimalfit.ui.screens.gym.tabs.CollectionTab
import eric.bitria.minimalfit.ui.screens.gym.tabs.HistoryTab
import eric.bitria.minimalfit.ui.viewmodels.gym.GymViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymScreen(
    onNavigateToSession: (String?) -> Unit,
    onNavigateToRoutineSession: (String, Boolean) -> Unit,
    onNavigateToExerciseProgression: (String) -> Unit,
    onNavigateToRoutine: (String?) -> Unit,
    viewModel: GymViewModel = koinViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val hasActiveWorkout by viewModel.hasActiveWorkout.collectAsState()

    ScreenConfiguration(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Workout",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            )
        },
        floatingActionButton = {
            PrimaryFloatingActionButton(
                onClick = { onNavigateToSession(null) },
                text = if (hasActiveWorkout) "Resume Workout" else "Start Workout",
                icon = if (hasActiveWorkout) Icons.Filled.PlayArrow else Icons.Filled.Add,
                contentDescription = if (hasActiveWorkout) "Resume workout" else "Start workout"
            )
        },
        bottomBar = true,
        quickActions = false
    )

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                text = { Text("Collection") }
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                text = { Text("History") }
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> CollectionTab(
                    onNavigateToRoutine = onNavigateToRoutine,
                    onNavigateToRoutineSession = onNavigateToRoutineSession,
                    onNavigateToExerciseProgression = onNavigateToExerciseProgression
                )
                1 -> HistoryTab(
                    onNavigateToSession = onNavigateToSession
                )
            }
        }
    }
}
