package eric.bitria.minimalfit.ui.components.gym.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.ui.components.gym.dialogs.SetPickerDialog
import eric.bitria.minimalfit.ui.components.gym.rows.SessionSetRow
import eric.bitria.minimalfit.ui.components.shared.animations.SwipeToDeleteCard
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.util.hourMinute
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun SessionExerciseCard(
    exerciseName: String,
    sets: List<Set>,
    isCollapsed: Boolean,
    canEdit: Boolean,
    createdAt: Instant,
    onToggleCollapse: () -> Unit,
    onUpdateSet: (Set) -> Unit,
    onDeleteSet: (String) -> Unit,
    onAddSet: (weight: Float, reps: Int, isCompleted: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddSetDialog by remember { mutableStateOf(false) }

    val addedTime = createdAt
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .time
        .hourMinute()
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (isCollapsed)
                MaterialTheme.colorScheme.surfaceContainerLow
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(Spacing.m)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleCollapse),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exerciseName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = addedTime,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                if (isCollapsed) {
                    val totalWeight = sets.sumOf { (it.weight * it.reps).toDouble() }
                    Text(
                        text = "${sets.size} sets • ${totalWeight.toInt()} kg",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (!isCollapsed) {
                Spacer(modifier = Modifier.height(Spacing.m))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.m),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    Spacer(modifier = Modifier.width(Spacing.m))
                    Text(
                        text = "KG",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Text(
                        text = "REPS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "DONE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(Spacing.xxl)
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                Column(modifier = Modifier.fillMaxWidth()) {
                    sets.forEachIndexed { index, set ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = Spacing.xs),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        }
                        if (canEdit) {
                            SwipeToDeleteCard(
                                onDismiss = {},
                                onDeleteRequested = { onDeleteSet(set.id) },
                                modifier = Modifier.clip(MaterialTheme.shapes.medium)
                            ) {
                                SessionSetRow(
                                    index = index,
                                    set = set,
                                    onUpdate = onUpdateSet,
                                    isActive = true
                                )
                            }
                        } else {
                            SessionSetRow(
                                index = index,
                                set = set,
                                onUpdate = {},
                                isActive = false
                            )
                        }
                    }
                }

                if (canEdit) {
                    Spacer(modifier = Modifier.height(Spacing.s))
                    Button(
                        onClick = { showAddSetDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Add Set")
                    }
                }
            }
        }
    }

    if (showAddSetDialog) {
        val lastSet = sets.lastOrNull()
        SetPickerDialog(
            title = "Add Set",
            weight = lastSet?.weight ?: 0f,
            reps = lastSet?.reps ?: 0,
            onDismiss = { showAddSetDialog = false },
            onConfirm = { w, r, completed ->
                onAddSet(w, r, completed)
                showAddSetDialog = false
            },
            showCompleted = true
        )
    }
}
