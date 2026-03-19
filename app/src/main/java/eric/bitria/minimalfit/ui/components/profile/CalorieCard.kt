package eric.bitria.minimalfit.ui.components.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    BoxWithConstraints(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(containerColor)
            .padding(Spacing.m)
    ) {
        val minDimension = minOf(maxWidth, maxHeight)
        val scaleFactor = (minDimension.value / 160f).coerceAtLeast(0.5f)

        // =========================
        // Background arcs
        // =========================
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.minDimension * 0.18f
            val spacing = strokeWidth * 1.25f

            val arcCenterX = -size.width * 0.15f
            val arcCenterY = size.height * 1.05f

            val eatenRadius = size.minDimension * 1.0f
            val eatenTopLeft = Offset(
                arcCenterX - eatenRadius,
                arcCenterY - eatenRadius
            )

            drawArc(
                color = contentColor.copy(alpha = 0.12f),
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                topLeft = eatenTopLeft,
                size = Size(eatenRadius * 2, eatenRadius * 2)
            )
            drawArc(
                color = contentColor,
                startAngle = 270f,
                sweepAngle = 90f * eatenProgress,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                topLeft = eatenTopLeft,
                size = Size(eatenRadius * 2, eatenRadius * 2)
            )

            val burnedRadius = eatenRadius - spacing
            val burnedTopLeft = Offset(
                arcCenterX - burnedRadius,
                arcCenterY - burnedRadius
            )

            drawArc(
                color = contentColor.copy(alpha = 0.08f),
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                topLeft = burnedTopLeft,
                size = Size(burnedRadius * 2, burnedRadius * 2)
            )
            drawArc(
                color = contentColor.copy(alpha = 0.6f),
                startAngle = 270f,
                sweepAngle = 90f * burnedProgress,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                topLeft = burnedTopLeft,
                size = Size(burnedRadius * 2, burnedRadius * 2)
            )
        }

        // =========================
        // Icon
        // =========================
        val iconSize = (28 * scaleFactor).dp

        StatCardIcon(
            icon = Icons.Outlined.LocalFireDepartment,
            contentColor = contentColor,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(iconSize)
        )

        // =========================
        // Content
        // =========================
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(
                    top = iconSize + (8 * scaleFactor).dp,
                    end = (10 * scaleFactor).dp
                ),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy((18 * scaleFactor).dp)
        ) {

            // ===== EATEN =====
            Column(horizontalAlignment = Alignment.End) {

                Text(
                    text = "Eaten",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.7f),
                    fontSize = (12 * scaleFactor).sp
                )

                val formattedEaten = remember(eaten) { "%,d".format(eaten) }
                val formattedGoal = remember(eatenGoal) { "%,d".format(eatenGoal) }

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = MaterialTheme.typography.displayMedium.toSpanStyle().copy(
                                fontWeight = FontWeight.Black,
                                color = contentColor
                            )
                        ) {
                            append(formattedEaten)
                        }

                        append(" ")

                        withStyle(
                            style = MaterialTheme.typography.titleMedium.toSpanStyle().copy(
                                color = contentColor.copy(alpha = 0.6f)
                            )
                        ) {
                            append("/ $formattedGoal")
                        }

                        append(" ")

                        withStyle(
                            style = MaterialTheme.typography.labelSmall.toSpanStyle().copy(
                                color = contentColor.copy(alpha = 0.5f)
                            )
                        ) {
                            append("kcal")
                        }
                    }
                )
            }

            // ===== BURNED =====
            Column(horizontalAlignment = Alignment.End) {

                Text(
                    text = "Burned",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.6f),
                    fontSize = (11 * scaleFactor).sp
                )

                val formattedBurned = remember(burned) { "%,d".format(burned) }
                val formattedBurnedGoal = remember(burnedGoal) { "%,d".format(burnedGoal) }

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = MaterialTheme.typography.headlineMedium.toSpanStyle().copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = contentColor.copy(alpha = 0.9f)
                            )
                        ) {
                            append(formattedBurned)
                        }

                        append(" ")

                        withStyle(
                            style = MaterialTheme.typography.bodyMedium.toSpanStyle().copy(
                                color = contentColor.copy(alpha = 0.6f)
                            )
                        ) {
                            append("/ $formattedBurnedGoal")
                        }

                        append(" ")

                        withStyle(
                            style = MaterialTheme.typography.labelSmall.toSpanStyle().copy(
                                color = contentColor.copy(alpha = 0.5f)
                            )
                        ) {
                            append("kcal")
                        }
                    }
                )
            }
        }
    }
}