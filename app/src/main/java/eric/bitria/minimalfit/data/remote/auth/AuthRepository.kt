package eric.bitria.minimalfit.data.remote.auth

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<FirebaseUser?>
    
    suspend fun login(email: String, password: String): Result<FirebaseUser?>
    suspend fun register(email: String, password: String): Result<FirebaseUser?>
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser?>
    suspend fun logout()
}
