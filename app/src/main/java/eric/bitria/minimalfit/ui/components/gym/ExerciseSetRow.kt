package eric.bitria.minimalfit.ui.components.gym

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
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
import eric.bitria.minimalfit.data.entity.gym.Set
import eric.bitria.minimalfit.ui.components.shared.WheelPicker
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.util.GYM_WEIGHT_VALUES
import eric.bitria.minimalfit.util.formatWeight
import eric.bitria.minimalfit.util.weightToIndex

private val WHEEL_ITEM_HEIGHT = 44.dp
private val WHEEL_VISIBLE_ITEMS = 5

@Composable
fun ExerciseSetRow(
    index: Int,
    set: Set,
    onUpdate: (Set) -> Unit,
    isActive: Boolean,
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
                    text = if (set.weight == 0f) "—" else formatWeight(set.weight),
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
                Text(
                    text = if (set.reps == 0) "—" else set.reps.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showEditDialog) {
        SetEditDialog(
            set = set,
            index = index,
            onDismiss = { showEditDialog = false },
            onConfirm = { updated ->
                onUpdate(updated)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun SetEditDialog(
    set: Set,
    index: Int,
    onDismiss: () -> Unit,
    onConfirm: (Set) -> Unit
) {
    var weightIndex by remember { mutableIntStateOf(weightToIndex(set.weight)) }
    var repsIndex by remember { mutableIntStateOf((set.reps - 1).coerceAtLeast(0)) }

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
                    text = "Set ${index + 1}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.l))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        WheelPicker(
                            count = GYM_WEIGHT_VALUES.size,
                            initialIndex = weightIndex,
                            onIndexChanged = { weightIndex = it },
                            visibleItems = WHEEL_VISIBLE_ITEMS,
                            itemHeight = WHEEL_ITEM_HEIGHT,
                            modifier = Modifier.width(100.dp)
                        ) { i, isSelected ->
                            Text(
                                text = formatWeight(GYM_WEIGHT_VALUES[i]),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                        Text(
                            text = "kg",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(WHEEL_ITEM_HEIGHT * WHEEL_VISIBLE_ITEMS)
                            .padding(horizontal = Spacing.m),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "×",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        WheelPicker(
                            count = 50,
                            initialIndex = repsIndex,
                            onIndexChanged = { repsIndex = it },
                            visibleItems = WHEEL_VISIBLE_ITEMS,
                            itemHeight = WHEEL_ITEM_HEIGHT,
                            modifier = Modifier.width(80.dp)
                        ) { i, isSelected ->
                            Text(
                                text = "${i + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                        Text(
                            text = "reps",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.l))

                Button(
                    onClick = {
                        onConfirm(
                            set.copy(
                                weight = GYM_WEIGHT_VALUES[weightIndex],
                                reps = repsIndex + 1
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}
