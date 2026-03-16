package eric.bitria.minimalfit.data.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidLocationSensor(
    private val context: Context
) : LocationSensor {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var currentInterval = 10_000L
    private var currentMinDistance = 5f

    private val _location = MutableStateFlow<Location?>(null)
    override val location: StateFlow<Location?> = _location.asStateFlow()

    private var isTracking: Boolean = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { _location.value = it }
        }
    }

    override fun startUpdates() {
        if (isTracking) return
        if (!hasLocationPermission()) return

        isTracking = true
        requestUpdates()
    }

    override fun stopUpdates() {
        if (!isTracking) return
        isTracking = false
        fusedClient.removeLocationUpdates(locationCallback)
        _location.value = null
    }

    override fun updateSamplingRate(intervalMillis: Long, minDistanceMeters: Float) {
        currentInterval = intervalMillis
        currentMinDistance = minDistanceMeters

        if (isTracking && hasLocationPermission()) {
            requestUpdates()
        }
    }

    private fun requestUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, currentInterval)
            .setMinUpdateDistanceMeters(currentMinDistance)
            .setWaitForAccurateLocation(false)
            .build()

        try {
            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}