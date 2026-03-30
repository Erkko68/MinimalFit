package eric.bitria.minimalfit.ui.components.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.data.track.RecordingState

@Composable
fun TrackingToolbar(
    modifier: Modifier = Modifier,
    state: RecordingState,
    onStartResume: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onCenterOnUser: () -> Unit,
    onCenterOnRoute: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // PRIMARY GROUP (recording controls)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (state) {
                    RecordingState.IDLE -> {
                        FilledIconButton(onClick = onStartResume) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Start")
                        }
                    }

                    RecordingState.RECORDING -> {
                        FilledIconButton(onClick = onPause) {
                            Icon(Icons.Filled.Pause, contentDescription = "Pause")
                        }

                        FilledIconButton(
                            onClick = onStop,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(Icons.Filled.Stop, contentDescription = "Stop")
                        }
                    }

                    RecordingState.PAUSED -> {
                        FilledIconButton(onClick = onStartResume) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Resume")
                        }

                        FilledIconButton(
                            onClick = onStop,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(Icons.Filled.Stop, contentDescription = "Stop")
                        }
                    }
                }
            }

            // SECONDARY GROUP (map controls)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilledTonalIconButton(onClick = onCenterOnUser) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Center on User")
                }

                FilledTonalIconButton(onClick = onCenterOnRoute) {
                    Icon(Icons.Default.Map, contentDescription = "Center on Route")
                }
            }
        }
    }
}