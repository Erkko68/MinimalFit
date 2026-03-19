package eric.bitria.minimalfit.ui.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.ui.theme.Spacing

@Composable
fun GymCard(
    modifier: Modifier = Modifier,
    weight: String = "12,450",
    comparison: String = "+12% vs last week"
) {
    val containerColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(containerColor)
            .padding(Spacing.m)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalAlignment = Alignment.Top
        ) {
            StatCardIcon(
                icon = Icons.Outlined.BarChart,
                contentColor = contentColor
            )

            Column {
                Text(
                    text = "Gym Performance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.9f)
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = weight,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = contentColor
                    )
                    Text(
                        text = " kg",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Light,
                        color = contentColor,
                        modifier = Modifier.padding(bottom = Spacing.xs)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                    modifier = Modifier.padding(top = Spacing.s)
                ) {
                    StatCardChip(
                        text = "This Week",
                        containerColor = contentColor.copy(alpha = 0.2f),
                        contentColor = contentColor
                    )
                    Text(
                        comparison,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF86EFAC)
                    )
                }
            }
        }
    }
}
