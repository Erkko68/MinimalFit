package eric.bitria.minimalfit.data.sensor

import android.location.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LocationSensor {
    val location: StateFlow<Location>
    val isTracking: Boolean
    val isGpsEnabled: Flow<Boolean>
    val hasPermission: Boolean

    fun startUpdates()
    fun stopUpdates()

    /**
     * Dynamically adjusts the sampling rate without completely stopping the sensor.
     */
    fun updateSamplingRate(intervalMillis: Long, minDistanceMeters: Float)
}