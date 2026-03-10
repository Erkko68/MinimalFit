package eric.bitria.minimalfit.data.sensor

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

class AndroidLocationSensor(
    private val context: Context
) : LocationSensor {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var currentInterval = 10_000L
    private var currentMinDistance = 5f

    private val _location = MutableStateFlow<Location?>(null)
    override val location: StateFlow<Location?> = _location.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            _location.value = result.lastLocation
        }
    }

    override fun startUpdates() {
        if (_isTracking.value) return
        if (!hasLocationPermission()) return

        _isTracking.value = true
        requestUpdates()
    }

    override fun stopUpdates() {
        if (!_isTracking.value) return
        _isTracking.value = false
        fusedClient.removeLocationUpdates(locationCallback)
        _location.value = null
    }

    override fun updateSamplingRate(intervalMillis: Long, minDistanceMeters: Float) {
        currentInterval = intervalMillis
        currentMinDistance = minDistanceMeters

        // If we are currently tracking, update the request immediately
        if (_isTracking.value && hasLocationPermission()) {
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
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override val isGpsEnabled: Flow<Boolean> = callbackFlow {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        fun checkGps() = LocationManagerCompat.isLocationEnabled(locationManager)

        trySend(checkGps())

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    trySend(checkGps())
                }
            }
        }

        context.registerReceiver(receiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
        awaitClose { context.unregisterReceiver(receiver) }
    }
}