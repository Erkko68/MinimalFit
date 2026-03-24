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
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import eric.bitria.minimalfit.ui.theme.Spacing

@Composable
fun CalorieCard(
    eaten: Int,
    eatenGoal: Int,
    burned: Int,
    burnedGoal: Int,
    modifier: Modifier = Modifier
) {
    val containerColor = MaterialTheme.colorScheme.tertiary
    val contentColor = MaterialTheme.colorScheme.onTertiary

    val eatenProgress =
        (eaten.toFloat() / eatenGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
    val burnedProgress =
        (burned.toFloat() / burnedGoal.coerceAtLeast(1)).coerceIn(0f, 1f)

    // Main Card Container changed to a Column
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(containerColor)
            .padding(Spacing.m)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Calorie Intake",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor.copy(alpha = 0.9f)
            )

            StatCardIcon(
                icon = Icons.Outlined.LocalFireDepartment,
                contentColor = contentColor
            )
        }

        // BOTTOM BOX: Arcs Drawn Behind & Right-Aligned Data
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val strokeWidth = size.minDimension * 0.22f
                    val spacing = strokeWidth * 1.25f

                    // Define how far the curve stretches into the box
                    val eatenRadius = size.minDimension * 0.9f
                    val burnedRadius = eatenRadius - spacing

                    // The amount to bleed outside the box so the rounded caps get clipped
                    val bleed = strokeWidth

                    // 1. Define the full curves using Path coordinates
                    val eatenPath = androidx.compose.ui.graphics.Path().apply {
                        // Start off-screen to the Left
                        moveTo(-bleed, size.height - eatenRadius)

                        // End point goes off-screen to the Bottom
                        quadraticTo(
                            x1 = eatenRadius,
                            y1 = size.height - eatenRadius,
                            x2 = eatenRadius,
                            y2 = size.height + bleed
                        )
                    }

                    // Shift the burned path down and left so it nests inside
                    val burnedPath = androidx.compose.ui.graphics.Path().apply {
                        // Start off-screen to the Left
                        moveTo(-bleed, size.height - burnedRadius)

                        // End point goes off-screen to the Bottom
                        quadraticTo(
                            x1 = burnedRadius,
                            y1 = size.height - burnedRadius,
                            x2 = burnedRadius,
                            y2 = size.height + bleed
                        )
                    }

                    // 2. Draw the full background tracks
                    drawPath(
                        path = eatenPath,
                        color = contentColor.copy(alpha = 0.12f),
                        style = Stroke(strokeWidth, cap = StrokeCap.Round) // Caps are now hidden by the box's clip
                    )
                    drawPath(
                        path = burnedPath,
                        color = contentColor.copy(alpha = 0.08f),
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )

                    // 3. Use PathMeasure to extract and draw just the progress segment
                    val pathMeasure = androidx.compose.ui.graphics.PathMeasure()

                    // Eaten Progress
                    pathMeasure.setPath(eatenPath, forceClosed = false)
                    val eatenProgressPath = androidx.compose.ui.graphics.Path()
                    pathMeasure.getSegment(
                        startDistance = 0f,
                        stopDistance = pathMeasure.length * eatenProgress,
                        destination = eatenProgressPath
                    )
                    drawPath(
                        path = eatenProgressPath,
                        color = contentColor,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )

                    // Burned Progress
                    pathMeasure.setPath(burnedPath, forceClosed = false)
                    val burnedProgressPath = androidx.compose.ui.graphics.Path()
                    pathMeasure.getSegment(
                        startDistance = 0f,
                        stopDistance = pathMeasure.length * burnedProgress,
                        destination = burnedProgressPath
                    )
                    drawPath(
                        path = burnedProgressPath,
                        color = contentColor.copy(alpha = 0.6f),
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }
        ) {
            // Data Text Column aligned to the End (Right) of the Box
            Column(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
                horizontalAlignment = Alignment.End
            ) {
                // ===== EATEN =====
                Column(horizontalAlignment = Alignment.End) {
                    val formattedEaten = remember(eaten) { "%,d".format(eaten) }
                    val formattedGoal = remember(eatenGoal) { "%,d".format(eatenGoal) }

                    Row {
                        Text(
                            text = formattedEaten,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = contentColor,
                            modifier = Modifier.alignByBaseline()
                        )
                        Text(
                            text = " / $formattedGoal kcal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor.copy(alpha = 0.5f),
                            modifier = Modifier.alignByBaseline()
                        )
                    }

                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = contentColor.copy(alpha = 0.8f))) {
                                append("Eaten")
                            }
                            append(" kcal")
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.6f)
                    )
                }

                // ===== BURNED =====
                Column(horizontalAlignment = Alignment.End) {
                    val formattedBurned = remember(burned) { "%,d".format(burned) }
                    val formattedBurnedGoal = remember(burnedGoal) { "%,d".format(burnedGoal) }

                    Row {
                        Text(
                            text = formattedBurned,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = contentColor.copy(alpha = 0.9f),
                            modifier = Modifier.alignByBaseline()
                        )
                        Text(
                            text = " / $formattedBurnedGoal kcal",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor.copy(alpha = 0.5f),
                            modifier = Modifier.alignByBaseline()
                        )
                    }

                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = contentColor.copy(alpha = 0.8f))) {
                                append("Burned")
                            }
                            append(" kcal")
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}