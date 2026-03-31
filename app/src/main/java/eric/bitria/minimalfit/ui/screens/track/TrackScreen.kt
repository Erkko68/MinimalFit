package eric.bitria.minimalfit.ui.screens.track

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.components.shared.animations.SwipeToDeleteCard
import eric.bitria.minimalfit.ui.components.food.actions.PrimaryFloatingActionButton
import eric.bitria.minimalfit.ui.components.track.cards.EmptyTrackPlaceholder
import eric.bitria.minimalfit.ui.components.track.cards.TrackCard
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.track.TrackViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TrackScreen(
    onTrackClick: (String) -> Unit = {},
    onNewTrackClick: () -> Unit = {},
    viewModel: TrackViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tracks = uiState.activities
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    ScreenConfiguration(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tracks",
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
                onClick = onNewTrackClick,
                text = "Start a new track"
            )
        },
        quickActions = false,
        bottomBar = true
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .padding(horizontal = Spacing.m),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                vertical = Spacing.s
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            item {
                Text(
                    text = "Past Tracks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            if (tracks.isEmpty()) {
                item {
                    EmptyTrackPlaceholder(
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
            items(tracks, key = { it.id }) { track ->
                SwipeToDeleteCard(
                    onDismiss = { viewModel.deleteActivity(track.id) },
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraLarge)
                        .animateItem()
                ) {
                    TrackCard(
                        track = track,
                        onClick = { onTrackClick(track.id) }
                    )
                }
            }
        }
    }
}
