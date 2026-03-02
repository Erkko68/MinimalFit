package eric.bitria.minimalfit.ui.components.food.search

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.data.model.Meal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPickerRow(
    meal: Meal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val width = maxWidth

        OutlinedCard(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
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
                    modifier = Modifier
                        .size(width * 0.1f)
                        .padding(end = width * 0.02f)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meal.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = meal.tags.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${meal.calories} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

