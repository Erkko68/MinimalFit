package eric.bitria.minimalfit.ui.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import eric.bitria.minimalfit.ui.theme.Spacing

@Composable
fun GymCard(
    modifier: Modifier = Modifier,
    weight: String = "12,450",
    comparison: String = "+12% vs last week"
) {
    val containerColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onPrimary

    // 1. Prepare the vector and color outside the layout phase
    val watermarkPainter = rememberVectorPainter(Icons.Outlined.FitnessCenter)
    val watermarkTint = contentColor.copy(alpha = 0.08f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(containerColor)
            .drawBehind {
                val iconSize = size.height * 1.6f

                // Calculate position: Push it off the right edge and center vertically
                val xOffset = size.width - (iconSize * 0.8f)
                val yOffset = (size.height - iconSize) / 4f

                translate(left = xOffset, top = yOffset) {
                    with(watermarkPainter) {
                        draw(
                            size = Size(iconSize, iconSize),
                            colorFilter = ColorFilter.tint(watermarkTint)
                        )
                    }
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.m),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Text Column (Left)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
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
                        color = contentColor,
                        modifier = Modifier.alignByBaseline()
                    )
                    Text(
                        text = " kg",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.6f),
                        modifier = Modifier.alignByBaseline()
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                    modifier = Modifier.padding(top = Spacing.xs)
                ) {
                    StatCardChip(
                        text = "This Week",
                        containerColor = contentColor.copy(alpha = 0.2f),
                        contentColor = contentColor
                    )
                    Text(
                        text = comparison,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF86EFAC)
                    )
                }
            }

            // Foreground Icon (Right)
            StatCardIcon(
                icon = Icons.Outlined.BarChart,
                contentColor = contentColor
            )
        }
    }
}