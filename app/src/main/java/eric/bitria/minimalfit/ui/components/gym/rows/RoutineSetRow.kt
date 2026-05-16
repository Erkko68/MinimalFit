package eric.bitria.minimalfit.ui.components.gym.rows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import eric.bitria.minimalfit.data.entity.gym.RoutineSet
import eric.bitria.minimalfit.ui.components.gym.dialogs.SetPickerDialog
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.util.formatWeight

@Composable
fun RoutineSetRow(
    index: Int,
    set: RoutineSet,
    onUpdate: (RoutineSet) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }

    val containerColor = MaterialTheme.colorScheme.surfaceVariant
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        modifier = modifier
            .fillMaxWidth()
            .clickable { showEditDialog = true }
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
        }
    }

    if (showEditDialog) {
        SetPickerDialog(
            title = "Set ${index + 1}",
            weight = set.weight,
            reps = set.reps,
            onDismiss = { showEditDialog = false },
            onConfirm = { w, r, type, duration, prep, _ ->
                onUpdate(
                    set.copy(
                        weight = w,
                        reps = r,
                        type = type,
                        durationSeconds = duration,
                        preparationSeconds = prep
                    )
                )
                showEditDialog = false
            },
            type = set.type,
            durationSeconds = set.durationSeconds.takeIf { it > 0 } ?: 30,
            preparationSeconds = set.preparationSeconds
        )
    }
}
