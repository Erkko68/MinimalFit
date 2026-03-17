package eric.bitria.minimalfit.ui.components.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.ui.theme.Spacing

@Composable
fun ProfileHeader(
    modifier: Modifier = Modifier,
    title: String = "Welcome\nBack!",
    subtitle: String = "Thursday, October 24"
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = MaterialTheme.typography.displayMedium.letterSpacing
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = Spacing.s)
        )
    }
}

@Composable
fun CalorieTrackerCard(
    modifier: Modifier = Modifier,
    calories: String = "1,820",
    goal: String = "2,400",
    progress: Float = 0.75f
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        // Simulated Image Mask
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Top Right Heart Icon
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(Spacing.m)
                .clip(RoundedCornerShape(Spacing.m))
                .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
                .padding(Spacing.s)
        ) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorite",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // Quarter Circle Tracker
        val primaryColor = MaterialTheme.colorScheme.primary
        Canvas(modifier = Modifier.fillMaxSize()) {
            val arcSize = size.height * 1.5f
            val strokeWidth = 36f

            drawArc(
                color = primaryColor.copy(alpha = 0.1f),
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                topLeft = Offset(-strokeWidth, size.height - arcSize + strokeWidth),
                size = Size(arcSize, arcSize)
            )

            drawArc(
                color = primaryColor,
                startAngle = 270f,
                sweepAngle = 90f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(-strokeWidth, size.height - arcSize + strokeWidth),
                size = Size(arcSize, arcSize)
            )
        }

        // Calorie Data Text
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = Spacing.l, bottom = Spacing.m)
        ) {
            Text(
                text = calories,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "kcal eaten",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                modifier = Modifier.offset(y = (-Spacing.xs))
            )
            Text(
                text = "Goal: $goal",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun GymPerformanceCard(
    modifier: Modifier = Modifier,
    weight: String = "12,450",
    comparison: String = "+12% vs last week"
) {
    StatCard(
        modifier = modifier,
        title = "Gym Performance",
        value = weight,
        unit = "kg",
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        icon = Icons.Outlined.BarChart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            modifier = Modifier.padding(top = Spacing.s)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                    .padding(horizontal = Spacing.s, vertical = Spacing.xs)
            ) {
                Text(
                    "This Week",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                comparison,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF86EFAC) // Keep the green for positive trend
            )
        }
    }
}

@Composable
fun MorningRunCard(
    modifier: Modifier = Modifier,
    distance: String = "8.42",
    duration: String = "45m 12s",
    pace: String = "5'20\" /km"
) {
    StatCard(
        modifier = modifier,
        title = "Morning Run",
        value = distance,
        unit = "km",
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
        icon = Icons.AutoMirrored.Outlined.DirectionsRun
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            modifier = Modifier.padding(top = Spacing.s)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.2f))
                    .padding(horizontal = Spacing.s, vertical = Spacing.xs)
            ) {
                Text(duration, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondary)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.2f))
                    .padding(horizontal = Spacing.s, vertical = Spacing.xs)
            ) {
                Text(pace, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondary)
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    containerColor: Color,
    contentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    extraContent: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(containerColor)
            .padding(Spacing.m)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.9f)
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = contentColor
                    )
                    Text(
                        text = " $unit",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Light,
                        color = contentColor,
                        modifier = Modifier.padding(bottom = Spacing.xs)
                    )
                }
                extraContent()
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Spacing.m))
                    .background(contentColor.copy(alpha = 0.1f))
                    .padding(Spacing.s)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor
                )
            }
        }
    }
}
