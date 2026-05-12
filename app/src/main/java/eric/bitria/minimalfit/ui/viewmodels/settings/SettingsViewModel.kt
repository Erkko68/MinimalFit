package eric.bitria.minimalfit.ui.viewmodels.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import eric.bitria.minimalfit.data.remote.auth.AuthRepository
import eric.bitria.minimalfit.data.remote.sync.SyncLogEntry
import eric.bitria.minimalfit.data.remote.sync.SyncLogStore
import eric.bitria.minimalfit.data.remote.sync.SyncOrchestrator
import eric.bitria.minimalfit.data.repository.user.UserPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    val showVerificationMessage: Boolean = false,
    val deleteError: String? = null
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val syncOrchestrator: SyncOrchestrator,
    private val syncLogStore: SyncLogStore
) : ViewModel() {

    val syncLogs: StateFlow<List<SyncLogEntry>> = syncLogStore.entries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSyncing = MutableStateFlow(false)
    private val _verificationCooldown = MutableStateFlow(0)
    private val _showVerificationMessage = MutableStateFlow(false)
    private val _deleteError = MutableStateFlow<String?>(null)
    private var verificationTimerJob: Job? = null
    private var autoReloadJob: Job? = null

    val uiState: StateFlow<SettingsUiState> = combine(
        authRepository.currentUser,
        _isSyncing,
        userPreferencesRepository.isAutoSyncEnabled,
        _verificationCooldown,
        _showVerificationMessage,
        _deleteError
    ) { args: Array<Any?> ->
        val user = args[0] as? FirebaseUser
        val syncing = args[1] as Boolean
        val autoSync = args[2] as Boolean
        val cooldown = args[3] as Int
        val showMessage = args[4] as Boolean
        val deleteError = args[5] as? String

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
            showVerificationMessage = showMessage,
            deleteError = deleteError
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
                delay(5000)
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
        val user = authRepository.currentUser.value
        if (user != null && user.isEmailVerified) {
            viewModelScope.launch {
                _isSyncing.value = true
                syncOrchestrator.syncAll()
                _isSyncing.value = false
            }
        }
    }

    fun clearSyncLogs() { syncLogStore.clear() }

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

    fun reauthenticateAndDelete(password: String) {
        viewModelScope.launch {
            _deleteError.value = null
            authRepository.reauthenticateAndDelete(password).onFailure { e ->
                _deleteError.value = when {
                    e.message?.contains("password", ignoreCase = true) == true ||
                    e.message?.contains("credential", ignoreCase = true) == true ||
                    e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> "Incorrect password."
                    else -> "Failed to delete account. Please try again."
                }
            }
        }
    }

    fun clearDeleteError() { _deleteError.value = null }

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
