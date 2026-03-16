package eric.bitria.minimalfit.ui.components.settings

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import eric.bitria.minimalfit.ui.components.permission.PermissionDialog

@Composable
fun RequireLocationEnabled(
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var isLocationEnabled by remember {
        mutableStateOf(checkLocationEnabled(context))
    }

    // Check when returning to the app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val enabled = checkLocationEnabled(context)
                isLocationEnabled = enabled
                if (enabled) {
                    onResult(true)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (!isLocationEnabled) {
        PermissionDialog(
            title = "Location Services Required",
            text = "Your device location is turned off. Please enable it in the system settings to continue recording.",
            showSettingsButton = true,
            onDismiss = { 
                onResult(false)
            },
            onConfirm = {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            }
        )
    } else {
        // If already enabled, report success
        onResult(true)
    }
}

private fun checkLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}
