package eric.bitria.minimalfit.ui.viewmodels.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import eric.bitria.minimalfit.data.remote.auth.AuthRepository
import eric.bitria.minimalfit.data.remote.sync.SyncRepository
import eric.bitria.minimalfit.data.repository.food.FoodCatalogRepository
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.RoutineExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.RoutineRepository
import eric.bitria.minimalfit.data.repository.gym.SessionExerciseRepository
import eric.bitria.minimalfit.data.repository.gym.SessionRepository
import eric.bitria.minimalfit.data.repository.gym.SetRepository
import eric.bitria.minimalfit.data.repository.user.UserPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserProfile(
    val name: String,
    val email: String,
    val isEmailVerified: Boolean = false,
    val profilePictureUrl: String? = null
)

data class SettingsUiState(
    val isLoggedIn: Boolean = false,
    val userProfile: UserProfile? = null,
    val isSyncing: Boolean = false,
    val isGoogleSignInLoading: Boolean = false,
    val authError: String? = null,
    val isAutoSyncEnabled: Boolean = false,
    val verificationCooldown: Int = 0,
    val showVerificationMessage: Boolean = false
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val syncRepository: SyncRepository,
    private val foodCatalogRepository: FoodCatalogRepository,
    private val exerciseRepository: ExerciseRepository,
    private val routineRepository: RoutineRepository,
    private val routineExerciseRepository: RoutineExerciseRepository,
    private val sessionRepository: SessionRepository,
    private val sessionExerciseRepository: SessionExerciseRepository,
    private val setRepository: SetRepository
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    private val _isGoogleSignInLoading = MutableStateFlow(false)
    private val _authError = MutableStateFlow<String?>(null)
    private val _verificationCooldown = MutableStateFlow(0)
    private val _showVerificationMessage = MutableStateFlow(false)
    private var verificationTimerJob: Job? = null
    private var autoReloadJob: Job? = null

    val uiState: StateFlow<SettingsUiState> = combine(
        authRepository.currentUser,
        _isSyncing,
        _isGoogleSignInLoading,
        _authError,
        userPreferencesRepository.isAutoSyncEnabled,
        _verificationCooldown,
        _showVerificationMessage
    ) { args: Array<Any?> ->
        val user = args[0] as? FirebaseUser
        val syncing = args[1] as Boolean
        val googleLoading = args[2] as Boolean
        val authError = args[3] as String?
        val autoSync = args[4] as Boolean
        val cooldown = args[5] as Int
        val showMessage = args[6] as Boolean

        if (user != null && !user.isEmailVerified && autoReloadJob == null) {
            startAutoReload()
        } else if ((user == null || user.isEmailVerified) && autoReloadJob != null) {
            stopAutoReload()
        }

        SettingsUiState(
            isLoggedIn = user != null,
            userProfile = user?.let {
                UserProfile(
                    name = it.displayName ?: "User",
                    email = it.email ?: "",
                    isEmailVerified = it.isEmailVerified,
                    profilePictureUrl = it.photoUrl?.toString()
                )
            },
            isSyncing = syncing,
            isGoogleSignInLoading = googleLoading,
            authError = authError,
            isAutoSyncEnabled = autoSync,
            verificationCooldown = cooldown,
            showVerificationMessage = showMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    private fun startAutoReload() {
        autoReloadJob?.cancel()
        autoReloadJob = viewModelScope.launch {
            while (true) {
                delay(5000) // Poll every 5 seconds if not verified
                authRepository.reloadUser()
            }
        }
    }

    private fun stopAutoReload() {
        autoReloadJob?.cancel()
        autoReloadJob = null
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun onGoogleLoginSuccess(idToken: String) {
        viewModelScope.launch {
            _isGoogleSignInLoading.value = true
            _authError.value = null
            val result = authRepository.signInWithGoogle(idToken)
            _isGoogleSignInLoading.value = false
            result.onFailure { error ->
                _authError.value = error.message ?: "Google sign-in failed"
            }
        }
    }

    fun onGoogleLoginError(message: String) {
        _authError.value = message
    }

    fun uploadData() {
        val currentState = uiState.value
        val user = authRepository.currentUser.value
        if (user != null && user.isEmailVerified) {
            viewModelScope.launch {
                _isSyncing.value = true
                
                // 1. Sync Global Data (Get latest from Firestore)
                syncRepository.getGlobalIngredients().onSuccess { globals ->
                    globals.forEach { foodCatalogRepository.addIngredient(it) }
                }
                syncRepository.getGlobalExercises().onSuccess { globals ->
                    globals.forEach { exerciseRepository.addExercise(it) }
                }

                // 2. Backup User Data (Upload local to Firestore)
                val localIngredients = foodCatalogRepository.getIngredients().first()
                syncRepository.uploadUserIngredients(user.uid, localIngredients)

                val localMeals = foodCatalogRepository.getMeals().first()
                syncRepository.uploadUserMeals(user.uid, localMeals)

                val localExercises = exerciseRepository.getExercises().first()
                syncRepository.uploadUserExercises(user.uid, localExercises)

                val localRoutines = routineRepository.getAll().first()
                val localRoutineExercises = localRoutines.flatMap { routine ->
                    routineExerciseRepository.getForRoutine(routine.id).first()
                }
                syncRepository.uploadUserRoutines(user.uid, localRoutines, localRoutineExercises)

                val localSessions = sessionRepository.getSessions(limit = -1).first()
                val localSessionExercises = sessionExerciseRepository.getAllSessionExercises().first()
                val localSets = setRepository.getAllSets().first()
                syncRepository.uploadUserGymSessions(
                    user.uid,
                    localSessions,
                    localSessionExercises,
                    localSets
                )

                _isSyncing.value = false
            }
        }
    }

    fun toggleAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setAutoSyncEnabled(enabled)
        }
    }

    fun sendVerificationEmail() {
        if (_verificationCooldown.value > 0) return

        viewModelScope.launch {
            _showVerificationMessage.value = true
            startVerificationCooldown()
            authRepository.sendEmailVerification()
        }
    }

    private fun startVerificationCooldown() {
        verificationTimerJob?.cancel()
        verificationTimerJob = viewModelScope.launch {
            _verificationCooldown.value = 30
            while (_verificationCooldown.value > 0) {
                delay(1000)
                _verificationCooldown.value -= 1
            }
        }
    }

    fun reloadUser() {
        viewModelScope.launch {
            authRepository.reloadUser()
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            authRepository.deleteAccount()
        }
    }

    fun sendPasswordReset() {
        uiState.value.userProfile?.email?.let { email ->
            viewModelScope.launch {
                authRepository.sendPasswordResetEmail(email)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoReload()
        verificationTimerJob?.cancel()
    }
}
