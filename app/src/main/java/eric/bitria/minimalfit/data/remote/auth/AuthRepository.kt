package eric.bitria.minimalfit.data.remote.auth

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<FirebaseUser?>
    
    suspend fun login(email: String, password: String): Result<FirebaseUser?>
    suspend fun register(email: String, password: String): Result<FirebaseUser?>
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser?>
    suspend fun logout()
    
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun sendEmailVerification(): Result<Unit>
    suspend fun reloadUser(): Result<Unit>
    suspend fun updatePassword(newPassword: String): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
    suspend fun reauthenticateAndDelete(password: String): Result<Unit>
}
