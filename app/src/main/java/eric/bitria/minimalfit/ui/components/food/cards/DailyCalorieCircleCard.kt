package eric.bitria.minimalfit.ui.components.food.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import eric.bitria.minimalfit.ui.viewmodels.food.DailyCalorieData

@Composable
fun DailyCalorieCircleCard(
    dailyData: DailyCalorieData,
    progress: Float,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val cardSize = maxHeight
        val indicatorSize = cardSize * 0.95f

        val formattedCalories = remember(dailyData.currentCalories) {
            "%,d".format(dailyData.currentCalories)
        }

        val calorieShadow = Shadow(
            color = Color.Black.copy(alpha = 0.15f),
            offset = Offset(0f, 1.5f),
            blurRadius = 4f
        )

        // Standard circular progress
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.size(indicatorSize),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 16.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Compact date row
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dailyData.dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.05.em
                )
                Text(
                    text = dailyData.dayNumber.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Calories as hero but balanced size
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = MaterialTheme.typography.headlineMedium.toSpanStyle().copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.01).em,
                            color = MaterialTheme.colorScheme.primary,
                            shadow = calorieShadow
                        )
                    ) {
                        append(formattedCalories)
                    }
                    append(" ")
                    withStyle(
                        style = MaterialTheme.typography.labelSmall.toSpanStyle().copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.03.em
                        )
                    ) {
                        append("kcal")
                    }
                }
            )
        }
    }
}