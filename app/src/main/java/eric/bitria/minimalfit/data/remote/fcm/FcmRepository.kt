package eric.bitria.minimalfit.data.remote.fcm

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class FcmRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions,
) {

    /** Fetches the current FCM token and saves it to Firestore for the logged-in user. */
    suspend fun saveFcmToken() {
        val uid = auth.currentUser?.uid ?: return
        val token = FirebaseMessaging.getInstance().token.await()
        firestore.collection("users").document(uid)
            .set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    /** Saves a freshly-rotated token (called from MinimalFitMessagingService). */
    suspend fun onTokenRefresh(newToken: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .set(mapOf("fcmToken" to newToken), com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    /**
     * Syncs notification preferences to Firestore so Cloud Functions can read them.
     * Path: users/{uid} → notifications: { dailyRun, weightMilestone }
     */
    suspend fun syncNotificationPreferences(dailyRun: Boolean, weightMilestone: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .set(
                mapOf("notifications" to mapOf(
                    "dailyRun" to dailyRun,
                    "weightMilestone" to weightMilestone
                )),
                com.google.firebase.firestore.SetOptions.merge()
            )
            .await()
    }

    /**
     * Calls the Cloud Function to trigger the weight milestone notification.
     * The app is responsible for passing the current day's total lifted weight.
     * The function de-duplicates: it only sends the notification once per day.
     */
    suspend fun checkWeightMilestone(totalKg: Double) {
        functions
            .getHttpsCallable("check_weight_milestone")
            .call(mapOf("totalKg" to totalKg))
            .await()
    }
}
