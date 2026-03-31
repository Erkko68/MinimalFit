package eric.bitria.minimalfit.ui.viewmodels.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
    val name: String,
    val email: String,
    val profilePictureUrl: String? = null
)

data class SettingsUiState(
    val isLoggedIn: Boolean = false,
    val userProfile: UserProfile? = null
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun login() {
        _uiState.value = SettingsUiState(
            isLoggedIn = true,
            userProfile = UserProfile(
                name = "Paco",
                email = "paco@example.com"
            )
        )
    }

    fun logout() {
        _uiState.value = SettingsUiState(
            isLoggedIn = false,
            userProfile = null
        )
    }
}
