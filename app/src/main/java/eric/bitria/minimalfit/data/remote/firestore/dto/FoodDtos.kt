package eric.bitria.minimalfit.data.remote.firestore.dto

import kotlinx.serialization.Serializable

// ── Ingredient ──────────────────────────────────────────────────────────────

@Serializable
data class IngredientDto(
    val id: String = "",
    val name: String = "",
    val baseCalories: Int = 0,
    val measurementUnit: String = "GRAMS",
    val imageUrl: String? = null,
    val isGlobal: Boolean = false,
    val creatorId: String? = null,
    val updatedAt: String = ""
)

// ── Meal ─────────────────────────────────────────────────────────────────────

/** An ingredient within a meal, with its amount. */
@Serializable
data class MealIngredientDto(
    val ingredient: IngredientDto = IngredientDto(),
    val amount: Float = 0f
)

/** A complete meal document. */
@Serializable
data class MealDocument(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val measurementUnit: String = "GRAMS",
    val ingredients: List<MealIngredientDto> = emptyList(),
    val updatedAt: String = ""
)

// ── Diet ─────────────────────────────────────────────────────────────────────

/** A meal within a diet, with its amount. */
@Serializable
data class DietMealDto(
    val meal: MealDocument = MealDocument(),
    val amount: Float = 1f
)

/** A complete diet document. */
@Serializable
data class DietDocument(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val meals: List<DietMealDto> = emptyList(),
    val updatedAt: String = ""
)

// ── MealLog ──────────────────────────────────────────────────────────────────

/** A meal within a meal log, with its amount. */
@Serializable
data class MealLogMealDto(
    val meal: MealDocument = MealDocument(),
    val amount: Float = 0f
)

/** A complete meal log document. */
@Serializable
data class MealLogDocument(
    val id: String = "",
    val createdAt: String = "",
    val meals: List<MealLogMealDto> = emptyList(),
    val updatedAt: String = ""
)
