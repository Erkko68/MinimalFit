package eric.bitria.minimalfit.data.repository.track

import android.location.Location
import eric.bitria.minimalfit.data.sensor.ActivitySensor
import eric.bitria.minimalfit.data.sensor.ActivityType
import eric.bitria.minimalfit.data.sensor.LocationSensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TrackingLocationRepository(
    private val locationSensor: LocationSensor,
    private val activitySensor: ActivitySensor,
    private val coroutineScope: CoroutineScope
) : LocationRepository {

    override val location: StateFlow<Location?> = locationSensor.location
    override val isTracking: Boolean by locationSensor::isTracking
    override val isGpsEnabled: Flow<Boolean> = locationSensor.isGpsEnabled
    override val hasPermission: Boolean by locationSensor::hasPermission

    init {
        coroutineScope.launch {
            activitySensor.currentActivity.collect { activity ->
                adjustSamplingRateForActivity(activity)
            }
        }
    }

    override fun startTracking() {
        locationSensor.startUpdates()
        activitySensor.startListening()
    }

    override fun stopTracking() {
        locationSensor.stopUpdates()
        activitySensor.stopListening()
    }

    private fun adjustSamplingRateForActivity(activity: ActivityType) {
        when (activity) {
            ActivityType.RUNNING -> 2000L to 3f
            ActivityType.WALKING -> 6000L to 5f
            ActivityType.ON_BICYCLE -> 1500L to 4f
            ActivityType.STILL -> 30000L to 10f
            ActivityType.UNKNOWN -> 4000L to 3f
        }
    }
}