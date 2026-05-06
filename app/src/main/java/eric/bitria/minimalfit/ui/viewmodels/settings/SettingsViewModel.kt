package eric.bitria.minimalfit.ui.viewmodels.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.remote.auth.AuthRepository
import eric.bitria.minimalfit.data.remote.sync.SyncRepository
import eric.bitria.minimalfit.data.repository.food.FoodCatalogRepository
import eric.bitria.minimalfit.data.repository.gym.ExerciseRepository
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
    val isAutoSyncEnabled: Boolean = false,
    val verificationCooldown: Int = 0,
    val showVerificationMessage: Boolean = false
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val syncRepository: SyncRepository,
    private val foodCatalogRepository: FoodCatalogRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    private val _verificationCooldown = MutableStateFlow(0)
    private val _showVerificationMessage = MutableStateFlow(false)
    private var verificationTimerJob: Job? = null
    private var autoReloadJob: Job? = null

    val uiState: StateFlow<SettingsUiState> = combine(
        authRepository.currentUser,
        _isSyncing,
        userPreferencesRepository.isAutoSyncEnabled,
        _verificationCooldown,
        _showVerificationMessage
    ) { user, syncing, autoSync, cooldown, showMessage ->
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
