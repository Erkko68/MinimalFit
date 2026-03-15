package eric.bitria.minimalfit.ui.components.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.data.track.RecordingState
import eric.bitria.minimalfit.ui.theme.Spacing


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
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (state) {
                RecordingState.IDLE -> {
                    Button(
                        onClick = onStartResume,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Start")
                        Spacer(Modifier.width(Spacing.s))
                        Text("START", fontWeight = FontWeight.Bold)
                    }
                }
                RecordingState.RECORDING -> {
                    FilledIconButton(
                        onClick = onPause,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Filled.Pause, contentDescription = "Pause")
                    }
                    Spacer(modifier = Modifier.width(Spacing.m))
                    FilledIconButton(
                        onClick = onStop,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Filled.Stop, contentDescription = "Stop")
                    }
                    Spacer(modifier = Modifier.width(Spacing.m))
                    FilledTonalIconButton(onClick = onCenterOnUser) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Center on User")
                    }
                    Spacer(modifier = Modifier.width(Spacing.s))
                    FilledTonalIconButton(onClick = onCenterOnRoute) {
                        Icon(Icons.Default.Map, contentDescription = "Center on Route")
                    }
                }
                RecordingState.PAUSED -> {
                    FilledIconButton(
                        onClick = onStartResume,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Resume")
                    }
                    Spacer(modifier = Modifier.width(Spacing.m))
                    FilledIconButton(
                        onClick = onStop,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Filled.Stop, contentDescription = "Stop")
                    }
                    Spacer(modifier = Modifier.width(Spacing.m))
                    FilledTonalIconButton(onClick = onCenterOnUser) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Center on User")
                    }
                    Spacer(modifier = Modifier.width(Spacing.s))
                    FilledTonalIconButton(onClick = onCenterOnRoute) {
                        Icon(Icons.Default.Map, contentDescription = "Center on Route")
                    }
                }
            }
        }
    }
}