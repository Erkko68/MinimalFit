package eric.bitria.minimalfit.ui.components.track.cards

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eric.bitria.minimalfit.ui.theme.Spacing
import eric.bitria.minimalfit.util.fullWeekdayName
import eric.bitria.minimalfit.util.monthDayYear
import eric.bitria.minimalfit.util.today

@Composable
fun NewTrackCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentDate = today()
    val dayName = currentDate.fullWeekdayName()
    val date = currentDate.monthDayYear()


    // subtle containerColor transition for pressed/interaction states
    val containerColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerColor)
                .padding(Spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Start a new track",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(Spacing.m))

            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Start new track",
                    modifier = Modifier.fillMaxWidth(0.5f),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}