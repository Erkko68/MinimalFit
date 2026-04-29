package eric.bitria.minimalfit.ui.viewmodels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eric.bitria.minimalfit.data.remote.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}$".toRegex()

    val isRegisterEnabled: StateFlow<Boolean> = combine(
        _email, _password, _confirmPassword, _isLoading
    ) { email, password, confirmPassword, loading ->
        email.matches(emailRegex) && 
        password.length >= 6 && 
        password == confirmPassword && 
        !loading
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onEmailChange(newValue: String) {
        _email.value = newValue
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
    }

    fun onConfirmPasswordChange(newValue: String) {
        _confirmPassword.value = newValue
    }

    fun onRegisterClick() {
        if (isRegisterEnabled.value) {
            _isLoading.value = true
            _error.value = null
            viewModelScope.launch {
                val result = authRepository.register(_email.value, _password.value)
                _isLoading.value = false
                result.onFailure { error ->
                    _error.value = error.message ?: "Registration failed"
                }
            }
        }
    }
}
