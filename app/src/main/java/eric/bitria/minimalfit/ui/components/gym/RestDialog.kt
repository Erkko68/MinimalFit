package eric.bitria.minimalfit.ui.components.gym

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import eric.bitria.minimalfit.ui.components.shared.WheelPicker
import eric.bitria.minimalfit.ui.theme.Spacing
import kotlin.time.Duration

@Composable
fun RestDialog(
    isRestRunning: Boolean,
    restRemaining: Duration,
    onDismiss: () -> Unit,
    onStartRest: (seconds: Int) -> Unit,
    onAddTime: (seconds: Int) -> Unit,
    onStopRest: () -> Unit
) {
    var wasRunning by remember { mutableStateOf(false) }
    LaunchedEffect(isRestRunning) {
        if (isRestRunning) wasRunning = true
        if (!isRestRunning && wasRunning) onDismiss()
    }

    fun formatCountdown(duration: Duration): String {
        val total = duration.inWholeSeconds.coerceAtLeast(0)
        return "%d:%02d".format(total / 60, total % 60)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.l),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isRestRunning) "Rest" else "Start Rest",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.l))

                if (isRestRunning) {
                    Text(
                        text = formatCountdown(restRemaining),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Spacing.l))

                    TimePicker(
                        initialTotalSeconds = 30,
                        onSecondsChanged = { /* updated via confirm */ }
                    ) { totalSeconds ->
                        Button(
                            onClick = { onAddTime(totalSeconds) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Time")
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.m))

                    Button(
                        onClick = { onStopRest(); onDismiss() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Stop Rest")
                    }
                } else {
                    TimePicker(
                        initialTotalSeconds = 60,
                        onSecondsChanged = { /* updated via confirm */ }
                    ) { totalSeconds ->
                        Button(
                            onClick = { onStartRest(totalSeconds) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Start")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Two side-by-side [WheelPicker]s for minutes and seconds.
 * [actionButton] receives the current total seconds and renders the confirm button.
 */
@Composable
private fun TimePicker(
    initialTotalSeconds: Int,
    onSecondsChanged: (Int) -> Unit,
    actionButton: @Composable (totalSeconds: Int) -> Unit
) {
    var selectedMinutes by remember { mutableIntStateOf(initialTotalSeconds / 60) }
    var selectedSeconds by remember { mutableIntStateOf(initialTotalSeconds % 60) }
    val totalSeconds = selectedMinutes * 60 + selectedSeconds

    LaunchedEffect(totalSeconds) { onSecondsChanged(totalSeconds) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Each wheel + its label in a column so labels auto-align under their wheel
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                WheelPicker(
                    count = 60,
                    initialIndex = selectedMinutes,
                    onIndexChanged = { selectedMinutes = it },
                    visibleItems = 3,
                    modifier = Modifier.width(72.dp)
                ) { index, isSelected ->
                    Text(
                        text = "%02d".format(index),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = ":",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = Spacing.s)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                WheelPicker(
                    count = 60,
                    initialIndex = selectedSeconds,
                    onIndexChanged = { selectedSeconds = it },
                    visibleItems = 3,
                    modifier = Modifier.width(72.dp)
                ) { index, isSelected ->
                    Text(
                        text = "%02d".format(index),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "sec",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.l))

        actionButton(totalSeconds)
    }
}
