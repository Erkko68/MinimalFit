package eric.bitria.minimalfit.ui.screens.track

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import eric.bitria.minimalfit.ui.components.animations.SwipeToDeleteCard
import eric.bitria.minimalfit.ui.components.track.cards.TrackCard
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.track.TrackViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TrackScreen(
    onTrackClick: (String) -> Unit = {},
    viewModel: TrackViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tracks = uiState.activities

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.m)
    ) {
        Text(
            text = "Your Track History",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.02).em,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.s)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            items(
                items = tracks,
                key = { track -> track.id }
            ) { track ->
                SwipeToDeleteCard(
                    onDismiss = { viewModel.deleteActivity(track.id) },
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(durationMillis = 300),
                        fadeOutSpec = tween(durationMillis = 300),
                        placementSpec = tween(durationMillis = 300)
                    )
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