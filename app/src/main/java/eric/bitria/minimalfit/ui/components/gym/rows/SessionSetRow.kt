package eric.bitria.minimalfit.ui.components.gym.rows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.ui.components.gym.dialogs.SetPickerDialog
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.util.formatWeight
import kotlinx.coroutines.delay

@Composable
fun SessionSetRow(
    index: Int,
    set: Set,
    onUpdate: (Set) -> Unit,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var countdownPhase by remember(set.id) { mutableStateOf<TimedCountdownPhase?>(null) }
    var remainingSeconds by remember(set.id) { mutableStateOf(0) }

    val containerColor = MaterialTheme.colorScheme.surfaceVariant
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        modifier = modifier
            .fillMaxWidth()
            .then(if (isActive) Modifier.clickable { showEditDialog = true } else Modifier)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = Spacing.m, vertical = Spacing.s)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor.copy(alpha = 0.5f),
                modifier = Modifier.width(Spacing.m)
            )

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = if (set.weight == 0f) "-" else formatWeight(set.weight),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
            }

            VerticalDivider(
                modifier = Modifier.padding(vertical = Spacing.xs),
                color = contentColor.copy(alpha = 0.2f)
            )

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (set.isTimed) "${set.durationSeconds}s" else if (set.reps == 0) "-" else set.reps.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        textAlign = TextAlign.Center
                    )
                    if (set.isTimed && set.preparationSeconds > 0) {
                        Text(
                            text = "${set.preparationSeconds}s prep",
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.width(Spacing.xxl),
                contentAlignment = Alignment.Center
            ) {
                if (set.isTimed && isActive && !set.isCompleted) {
                    if (countdownPhase == null) {
                        IconButton(
                            onClick = {
                                countdownPhase = if (set.preparationSeconds > 0) {
                                    TimedCountdownPhase.Preparation
                                } else {
                                    TimedCountdownPhase.Work
                                }
                                remainingSeconds = if (set.preparationSeconds > 0) {
                                    set.preparationSeconds
                                } else {
                                    set.durationSeconds
                                }
                            }
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Start timed set")
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = remainingSeconds.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (countdownPhase == TimedCountdownPhase.Preparation) "prep" else "go",
                                style = MaterialTheme.typography.labelSmall,
                                color = contentColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    Checkbox(
                        checked = set.isCompleted,
                        onCheckedChange = { checked ->
                            if (isActive) onUpdate(set.copy(isCompleted = checked))
                        },
                        enabled = isActive
                    )
                }
            }
        }
    }

    LaunchedEffect(countdownPhase, remainingSeconds) {
        val phase = countdownPhase ?: return@LaunchedEffect
        if (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds -= 1
        } else if (phase == TimedCountdownPhase.Preparation) {
            countdownPhase = TimedCountdownPhase.Work
            remainingSeconds = set.durationSeconds
        } else {
            countdownPhase = null
            onUpdate(set.copy(isCompleted = true))
        }
    }

    if (showEditDialog) {
        SetPickerDialog(
            title = "Set ${index + 1}",
            weight = set.weight,
            reps = set.reps,
            onDismiss = { showEditDialog = false },
            onConfirm = { w, r, type, duration, prep, completed ->
                onUpdate(
                    set.copy(
                        weight = w,
                        reps = r,
                        type = type,
                        durationSeconds = duration,
                        preparationSeconds = prep,
                        isCompleted = completed
                    )
                )
                showEditDialog = false
            },
            showCompleted = true,
            initialCompleted = set.isCompleted,
            type = set.type,
            durationSeconds = set.durationSeconds.takeIf { it > 0 } ?: 30,
            preparationSeconds = set.preparationSeconds
        )
    }
}

private enum class TimedCountdownPhase {
    Preparation,
    Work
}
