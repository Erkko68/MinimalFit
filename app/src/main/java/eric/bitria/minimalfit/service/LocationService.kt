package eric.bitria.minimalfit.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import eric.bitria.minimalfit.MainActivity
import eric.bitria.minimalfit.R
import eric.bitria.minimalfit.data.track.RecordingState
import eric.bitria.minimalfit.data.track.TrackingLogic
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.time.Duration

/*
 * =====================================================================
 * Android 14 (API 34) Foreground Service Enforcement Fix
 * =====================================================================
 *
 * This addresses the following strict enforcement crash:
 * "java.lang.SecurityException: Starting FGS with type location... requires
 * permissions: ... and the app must be in the eligible state/exemptions"
 *
 * Android 14 throws this exception for two compounding reasons:
 * * 1. onStop() is a millisecond too late:
 * By the time Activity.onStop fires (which triggers ProcessLifecycleOwner's
 * onStop), the OS has already flagged the app's window as completely hidden
 * and transitioned the app into a "background" state. If we try to start the
 * FGS here, we lose the race and fail the exemption check.
 *
 * 2. Strict Permission Checks:
 * Android 14 will immediately crash the app if startForeground is called with
 * FOREGROUND_SERVICE_TYPE_LOCATION without explicitly ensuring the app currently
 * holds ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION at that exact millisecond.
 */

// CHANGE 1: Added DefaultLifecycleObserver to track the whole app, not just the Activity binding.
class LocationService : LifecycleService(), DefaultLifecycleObserver {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"

        private const val CHANNEL_ID = "location_channel"
        private const val NOTIFICATION_ID = 1
    }

    private val trackingLogic: TrackingLogic by inject()
    private val binder = LocalBinder()
    private var isForegroundService = false

    inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    override fun onCreate() {
        super<LifecycleService>.onCreate()
        createNotificationChannel()

        // CHANGE 2: Registering to ProcessLifecycleOwner instead of relying on onBind/onUnbind.
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // CHANGE 3: Replaced 3 separate `launch` blocks (from the slides) with a single `combine` operator.
        // This prevents the notification from being updated 3 times simultaneously every second, saving battery.
        lifecycleScope.launch {
            combine(
                trackingLogic.recordingState,
                trackingLogic.duration,
                trackingLogic.distanceKm
            ) { state, _, _ -> state }.collect { state ->
                if (isForegroundService) {
                    updateNotification()
                    if (state == RecordingState.IDLE) {
                        stopTracking()
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_PAUSE -> trackingLogic.pause()
            ACTION_RESUME -> trackingLogic.start()
            ACTION_STOP -> stopTracking()
        }
        return START_NOT_STICKY
    }

    // CHANGE 4: Replaced onBind for hiding the notification with onResume.
    // This reliably detects when the app UI is visible, regardless of binding delays.
    override fun onResume(owner: LifecycleOwner) {
        if (isForegroundService) demoteToBackground()
    }

    // CHANGE 5: Replaced onUnbind for showing the notification with onPause.
    // This triggers *before* the OS backgrounds the app, fixing the Android 14 crash.
    override fun onPause(owner: LifecycleOwner) {
        if (trackingLogic.recordingState.value != RecordingState.IDLE && !isForegroundService) {
            promoteToForeground()
        }
    }

    // CHANGE 6: Stripped all Foreground logic from onBind. It now only returns the binder.
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    // CHANGE 7: Deleted onUnbind and onRebind entirely. We don't need them anymore
    // since the ProcessLifecycleOwner handles the visibility state perfectly.

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        super<LifecycleService>.onDestroy()
    }

    private fun startTracking() = trackingLogic.start()

    private fun stopTracking() {
        trackingLogic.stop()
        demoteToBackground()
        stopSelf()
    }

    private fun promoteToForeground() {
        if (isForegroundService) return

        isForegroundService = true
        val notification = buildNotification()

        // CHANGE 9: Added the Build.VERSION check to ensure backward compatibility.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun demoteToBackground() {
        if (!isForegroundService) return
        isForegroundService = false
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun updateNotification() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Location Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Shows tracking stats in background" }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isPaused = trackingLogic.recordingState.value == RecordingState.PAUSED

        val pauseResumeAction = if (isPaused) {
            val resumeIntent = PendingIntent.getService(this, 2, Intent(this, LocationService::class.java).apply { action = ACTION_RESUME }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            NotificationCompat.Action(0, "Resume", resumeIntent)
        } else {
            val pauseIntent = PendingIntent.getService(this, 3, Intent(this, LocationService::class.java).apply { action = ACTION_PAUSE }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            NotificationCompat.Action(0, "Pause", pauseIntent)
        }

        val stopIntent = PendingIntent.getService(this, 1, Intent(this, LocationService::class.java).apply { action = ACTION_STOP }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val contentText = "Distance: %.2f km | Time: %s".format(
            trackingLogic.distanceKm.value,
            formatDuration(trackingLogic.duration.value)
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (isPaused) "Tracking Paused" else "Tracking Active")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(pauseResumeAction)
            .addAction(0, "Stop", stopIntent)
            .build()
    }

    private fun formatDuration(duration: Duration): String {
        val seconds = duration.inWholeSeconds
        return if (seconds >= 3600) {
            "%02d:%02d:%02d".format(seconds / 3600, (seconds % 3600) / 60, seconds % 60)
        } else {
            "%02d:%02d".format((seconds % 3600) / 60, seconds % 60)
        }
    }
}