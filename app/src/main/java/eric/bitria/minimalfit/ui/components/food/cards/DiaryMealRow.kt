package eric.bitria.minimalfit.ui.components.food.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.data.model.MealEntry

@Composable
fun DiaryMealRow(
    entry: MealEntry,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val width = maxWidth
        val meal = entry.meal

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = width * 0.04f, vertical = width * 0.03f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = meal.icon,
                    contentDescription = null,
                    tint = meal.color,
                    modifier = Modifier.size(width * 0.1f)
                )
                Spacer(Modifier.width(width * 0.03f))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meal.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    val servingsLabel = if (entry.servings != 1f) " · ${entry.servings}x" else ""
                    Text(
                        text = "${entry.totalCalories} kcal$servingsLabel · ${meal.tags.joinToString(" · ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
