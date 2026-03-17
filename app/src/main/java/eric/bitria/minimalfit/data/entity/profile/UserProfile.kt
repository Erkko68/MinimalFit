package eric.bitria.minimalfit.data.entity.profile

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: String = "user_profile",
    val name: String = "User",
    val weight: Float = 70f, // in kg
    val height: Float = 180f, // in cm
    val age: Int = 25,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculate BMI using weight and height
     * BMI = weight (kg) / (height (m) ^ 2)
     */
    fun calculateBmi(): Float {
        val heightInMeters = height / 100f
        return weight / (heightInMeters * heightInMeters)
    }

    /**
     * Get BMI category
     */
    fun getBmiCategory(): String {
        val bmi = calculateBmi()
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
    }
}

