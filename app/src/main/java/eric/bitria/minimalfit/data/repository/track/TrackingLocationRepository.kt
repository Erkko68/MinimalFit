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
            ActivityType.RUNNING -> locationSensor.updateSamplingRate(3_000L, 2f)
            ActivityType.WALKING -> locationSensor.updateSamplingRate(10_000L, 5f)
            ActivityType.ON_BICYCLE -> locationSensor.updateSamplingRate(5_000L, 10f)
            ActivityType.STILL -> locationSensor.updateSamplingRate(30_000L, 10f)
            ActivityType.UNKNOWN -> locationSensor.updateSamplingRate(10_000L, 5f)
        }
    }
}