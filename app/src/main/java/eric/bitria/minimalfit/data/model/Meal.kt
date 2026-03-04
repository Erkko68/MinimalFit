package eric.bitria.minimalfit.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A saved meal in the user's personal meal library.
 */
data class Meal(
    val id: String,
    val name: String,
    val description: String,
    val tags: List<String>,
    val color: Color,
    val icon: ImageVector,
    val unitType: UnitType = UnitType.GRAMS,
    /** The serving/reference amount that nutritional values are based on (e.g. 100 for "per 100 g"). */
    val servingSize: Float = 100f,
    /** Nutritional values keyed by [Nutrient]. Only populated entries are stored. */
    val nutrition: Map<Nutrient, Float> = emptyMap()
) {
    /** Convenience accessor — returns 0 when calories are not set. */
    val calories: Int get() = nutrition[Nutrient.CALORIES]?.toInt() ?: 0
}
