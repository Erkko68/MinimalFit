package eric.bitria.minimalfit.data.model

/**
 * All trackable nutritional values for a meal.
 *
 * Each entry carries a display [label] and the default [unit] suffix.
 */
enum class Nutrient(val label: String, val unit: String) {
    CALORIES("Calories", "kcal"),
    PROTEIN("Protein", "g"),
    CARBS("Carbs", "g"),
    FAT("Fat", "g"),
    FIBER("Fiber", "g"),
    SUGAR("Sugar", "g"),
    SALT("Salt", "mg")
}

