package eric.bitria.minimalfit.data.sensor

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidActivitySensor(
    private val context: Context
) : ActivitySensor {

    companion object {
        private const val ACTION_ACTIVITY_UPDATE = "eric.bitria.minimalfit.ACTION_ACTIVITY_UPDATE"
        private const val DETECTION_INTERVAL_IN_MILLISECONDS = 10_000L
    }

    private val activityRecognitionClient = ActivityRecognition.getClient(context)

    private val _currentActivity = MutableStateFlow(ActivityType.UNKNOWN)
    override val currentActivity: StateFlow<ActivityType> = _currentActivity.asStateFlow()

    // Dynamically registered receiver to catch the PendingIntent broadcasts
    private val activityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ActivityRecognitionResult.hasResult(intent)) {
                val result = ActivityRecognitionResult.extractResult(intent)
                val mostProbableActivity = result?.mostProbableActivity

                mostProbableActivity?.let {
                    processAndroidActivity(it.type)
                }
            }
        }
    }

    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(ACTION_ACTIVITY_UPDATE).apply {
            setPackage(context.packageName)
        }
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    override fun startListening() {
        if (!hasActivityRecognitionPermission()) return

        // 1. Register the local receiver
        val filter = IntentFilter(ACTION_ACTIVITY_UPDATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(activityReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                context,
                activityReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        // 2. Request updates from Google Play Services
        activityRecognitionClient.requestActivityUpdates(
            DETECTION_INTERVAL_IN_MILLISECONDS,
            pendingIntent
        ).addOnFailureListener { e ->
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    override fun stopListening() {
        if (!hasActivityRecognitionPermission()) return

        // 1. Stop updates from Google Play Services
        activityRecognitionClient.removeActivityUpdates(pendingIntent)

        // 2. Unregister the receiver to prevent memory leaks
        try {
            context.unregisterReceiver(activityReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered
        }

        _currentActivity.value = ActivityType.UNKNOWN
    }

    fun processAndroidActivity(detectedActivityType: Int) {
        _currentActivity.value = when (detectedActivityType) {
            DetectedActivity.ON_BICYCLE -> ActivityType.ON_BICYCLE
            DetectedActivity.RUNNING -> ActivityType.RUNNING
            DetectedActivity.WALKING -> ActivityType.WALKING
            DetectedActivity.STILL -> ActivityType.STILL
            else -> ActivityType.UNKNOWN
        }
    }

    private fun hasActivityRecognitionPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    }
}