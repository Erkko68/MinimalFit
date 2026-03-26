package eric.bitria.minimalfit.ui.components.track.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.em
import kotlin.time.Duration

@Composable
fun FloatingStats(
    distanceKm: Double,
    duration: Duration,
    modifier: Modifier = Modifier
) {
    // Replaced ConfigurationCompat with Compose's native Locale
    val locale = Locale.current.platformLocale

    val hours = duration.inWholeHours
    val minutes = (duration.inWholeMinutes % 60)
    val seconds = (duration.inWholeSeconds % 60)

    // Time format generally stays the same across locales, but using the platform
    // locale ensures consistency if there are any specific numeral systems in use.
    val timeString = if (hours > 0) {
        String.format(locale, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(locale, "%02d:%02d", minutes, seconds)
    }

    val mapTextShadow = Shadow(
        color = Color.Black.copy(alpha = 0.6f),
        offset = Offset(0f, 4f),
        blurRadius = 12f
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = timeString,
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.04).em,
                shadow = mapTextShadow
            ),
            color =  MaterialTheme.colorScheme.onSurface
        )
        Text(
            // This will now correctly format decimals based on the user's system
            // settings (e.g., "1,25 km" in Europe vs "1.25 km" in the US).
            text = String.format(locale, "%.2f km", distanceKm),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                shadow = mapTextShadow
            ),
            color = MaterialTheme.colorScheme.primaryContainer
        )
    }
}