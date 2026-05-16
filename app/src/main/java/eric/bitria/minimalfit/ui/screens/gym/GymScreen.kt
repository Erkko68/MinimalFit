package eric.bitria.minimalfit.ui.screens.gym

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.components.food.actions.PrimaryFloatingActionButton
import eric.bitria.minimalfit.ui.screens.gym.tabs.CollectionTab
import eric.bitria.minimalfit.ui.screens.gym.tabs.HistoryTab
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymScreen(
    onNavigateToSession: (String?) -> Unit,
    onNavigateToRoutineSession: (String, Boolean) -> Unit,
    onNavigateToExerciseProgression: (String) -> Unit,
    onNavigateToRoutine: (String?) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

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
                text = "Start Workout"
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
