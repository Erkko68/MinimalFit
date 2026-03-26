package eric.bitria.minimalfit.ui.screens.track

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import eric.bitria.minimalfit.navigation.ScreenConfiguration
import eric.bitria.minimalfit.ui.components.track.map.TrackMap
import eric.bitria.minimalfit.ui.components.track.map.fitRoute
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.ui.viewmodels.track.TrackDetailViewModel
import eric.bitria.minimalfit.util.hourMinute
import eric.bitria.minimalfit.util.weekdayMonthDay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.maplibre.compose.camera.rememberCameraState

@Composable
fun TrackDetailScreen(
    trackId: String,
    onNavigateBack: () -> Unit,
    viewModel: TrackDetailViewModel = koinViewModel { parametersOf(trackId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val track = uiState.track
    var editedName by remember(track?.name) { mutableStateOf(track?.name ?: "") }
    val cameraState = rememberCameraState()

    ScreenConfiguration(
        bottomBar = false,
        quickActions = false,
        fullScreen = true
    )

    if (track != null) {
        LaunchedEffect(track.routePoints) {
            cameraState.fitRoute(track.routePoints)
        }

        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {

            TrackMap(
                routePoints = track.routePoints,
                cameraState = cameraState,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = Spacing.m)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.s),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }

                    FilledTonalIconButton(
                        onClick = {
                            viewModel.deleteTrack()
                            onNavigateBack()
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Track")
                    }
                }

                BasicTextField(
                    value = editedName,
                    onValueChange = {
                        editedName = it
                        viewModel.updateTrackName(it)
                    },
                    textStyle = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        color =  MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.03).em
                    ),
                    cursorBrush = SolidColor( MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth()
                )

                val dateTime = "${track.date.weekdayMonthDay()} • ${track.time.hourMinute()}"

                Text(
                    text = dateTime,
                    style = MaterialTheme.typography.titleLarge,
                    color =  MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = Spacing.m)
                    .padding(bottom = Spacing.l),
                verticalArrangement = Arrangement.spacedBy(Spacing.m)
            ) {
                // The "Glass" Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    tonalElevation = Spacing.s,
                    shadowElevation = 12.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(Spacing.m)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val distanceText = "%.2f".format(track.distance)

                        ExpressiveStat(
                            label = "Distance",
                            value = distanceText,
                            unit = "km"
                        )

                        VerticalDivider(
                            modifier = Modifier.height(Spacing.xxl), // Uses 48.dp
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )

                        ExpressiveStat(label = "Duration", value = track.duration.toString(), unit = "")

                        VerticalDivider(
                            modifier = Modifier.height(Spacing.xxl), // Uses 48.dp
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )

                        ExpressiveStat(label = "Pace", value = track.pace, unit = "/km")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpressiveStat(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = " $unit",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = Spacing.xs),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}
