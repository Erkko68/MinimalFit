package eric.bitria.minimalfit.ui.components.requirements.settings

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import eric.bitria.minimalfit.ui.components.requirements.permission.PermissionDialog

/**
 * Monitors internet connectivity in real-time and manages a "No Internet" dialog.
 * 
 * @param showDialogOnLost Whether to show a dialog when connection is lost.
 * @return The current connection status as a [Boolean].
 */
@Composable
fun rememberInternetConnection(
    showDialogOnLost: Boolean = true
): Boolean {
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf(checkInitialConnection(context)) }
    var showNoInternetDialog by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isConnected = true
                showNoInternetDialog = false
            }

            override fun onLost(network: Network) {
                isConnected = false
                if (showDialogOnLost) showNoInternetDialog = true
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                isConnected = hasInternet
                if (hasInternet) showNoInternetDialog = false
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    if (showNoInternetDialog && !isConnected) {
        PermissionDialog(
            title = "No Internet Connection",
            text = "This feature requires an active internet connection. Please enable Wi-Fi or Cellular Data to continue.",
            showSettingsButton = true,
            onDismiss = { showNoInternetDialog = false },
            onConfirm = {
                showNoInternetDialog = false
                context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            }
        )
    }

    return isConnected
}

private fun checkInitialConnection(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
