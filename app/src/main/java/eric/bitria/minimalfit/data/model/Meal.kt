package eric.bitria.minimalfit.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A saved meal in the user's personal meal library.
 */
data class Meal(
    val id: String,
    val name: String,
    val calories: Int,
    val description: String,
    val tags: List<String>,
    val color: Color,
    val icon: ImageVector
)

