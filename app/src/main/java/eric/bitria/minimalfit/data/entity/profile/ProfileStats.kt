package eric.bitria.minimalfit.data.entity.profile

/**
 * Represents daily stats for the profile
 */
data class ProfileStats(
    val steps: Int = 8500,
    val calories: Int = 2100,
    val activeMinutes: Int = 45,
    val distance: Float = 6.5f // in km
)

