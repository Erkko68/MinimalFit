package eric.bitria.minimalfit.ui.components.shared.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import kotlin.math.max

@Composable
fun CalorieCircularProgressIndicator(
    progress: Float,
    dayLabel: String,
    dayNumber: Int,
    formattedCalories: String,
    modifier: Modifier = Modifier
) {
    val strokeWidth = 12.dp
    val internalPadding = 24.dp

    Layout(
        modifier = modifier.wrapContentSize(),
        content = {
            CircularProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = strokeWidth,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(internalPadding)
            ) {
                // Compact date row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.05.em
                    )
                    Text(
                        text = dayNumber.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Calories as hero but balanced size
                Text(
                    text = formattedCalories,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.01).em,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    ) { measurables, constraints ->
        val textPlaceable = measurables[1].measure(constraints.copy(minWidth = 0, minHeight = 0))
        
        // The diameter should be enough to contain the text + its internal padding + stroke width
        val contentSize = max(textPlaceable.width, textPlaceable.height)
        val diameter = contentSize + (strokeWidth.toPx() * 2).toInt()
        
        val indicatorPlaceable = measurables[0].measure(Constraints.fixed(diameter, diameter))

        layout(diameter, diameter) {
            indicatorPlaceable.place(0, 0)
            textPlaceable.place(
                (diameter - textPlaceable.width) / 2,
                (diameter - textPlaceable.height) / 2
            )
        }
    }
}