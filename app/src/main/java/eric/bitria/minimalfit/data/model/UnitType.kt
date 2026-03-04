package eric.bitria.minimalfit.data.model

/**
 * The unit system used to measure a meal serving.
 *
 * - [GRAMS]   – metric weight  (g, kg)
 * - [LITERS]  – metric volume  (mL, L)
 * - [IMPERIAL] – imperial units (oz, lb, fl oz, cups)
 * - [SERVING] – abstract serving count (used when no specific unit applies)
 */
enum class UnitType(val label: String, val symbol: String) {
    GRAMS(label = "Grams", symbol = "g"),
    MILLILITERS(label = "Milliliters", symbol = "mL"),
    IMPERIAL(label = "Imperial", symbol = "oz"),
    SERVING(label = "Serving", symbol = "srv")
}

