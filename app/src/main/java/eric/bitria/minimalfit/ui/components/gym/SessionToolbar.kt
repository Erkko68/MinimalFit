package eric.bitria.minimalfit.ui.components.gym

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
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

enum class SessionState {
    /** No session active — show Start only. */
    IDLE,
    /** Loaded a past session for viewing — show Resume only (no Stop). */
    VIEWING,
    /** Session running — show Pause, Stop, Rest, Add Exercise. */
    RUNNING,
    /** Session actively paused by user — show Resume and Stop. */
    PAUSED
}

@Composable
fun SessionToolbar(
    state: SessionState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onAddExercise: () -> Unit,
    onStartRest: () -> Unit,
    modifier: Modifier = Modifier
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (state) {
                SessionState.IDLE -> {
                    FilledIconButton(onClick = onStart) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Start")
                    }
                }

                SessionState.VIEWING -> {
                    FilledIconButton(onClick = onResume) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Resume")
                    }
                }

                SessionState.RUNNING -> {
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
                    FilledTonalIconButton(onClick = onStartRest) {
                        Icon(Icons.Filled.Timer, contentDescription = "Timer")
                    }
                    FilledTonalIconButton(onClick = onAddExercise) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Exercise")
                    }
                }

                SessionState.PAUSED -> {
                    FilledIconButton(onClick = onResume) {
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
    }
}
