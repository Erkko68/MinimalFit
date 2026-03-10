package eric.bitria.minimalfit.data.sensor

import kotlinx.coroutines.flow.StateFlow

interface ActivitySensor {
    val currentActivity: StateFlow<ActivityType>

    fun startListening()
    fun stopListening()
}