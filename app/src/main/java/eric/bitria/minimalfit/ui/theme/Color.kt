package eric.bitria.minimalfit.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Generic accent color
val Quaternary = Color(0xFF2196F3) // Vivid Blue

/**
 * Extension to create a vertical gradient brush from a single color
 * by lightening and darkening it slightly.
 */
fun Color.toVerticalGradient(): Brush {
    val light = lerp(this, Color.White, 0.15f)
    val dark = lerp(this, Color.Black, 0.15f)
    return Brush.verticalGradient(
        colors = listOf(light, dark)
    )
}
