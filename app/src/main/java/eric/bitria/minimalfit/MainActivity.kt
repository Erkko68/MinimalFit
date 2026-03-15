package eric.bitria.minimalfit

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import eric.bitria.minimalfit.service.LocationService
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    private var locationService: LocationService? = null
    private var isBound = false

    // ── Service Connection ───────────────────────────────────────────────────
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            locationService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            App()
        }
    }

    // ── Lifecycle Callbacks ──────────────────────────────────────────────────
    override fun onStart() {
        super.onStart()
        // Bind to the service when the activity is visible
        Intent(this, LocationService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        // Unbind when the activity is no longer visible
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}
