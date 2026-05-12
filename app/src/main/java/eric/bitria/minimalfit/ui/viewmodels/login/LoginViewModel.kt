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

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}$".toRegex()

    val isLoginEnabled: StateFlow<Boolean> = combine(_email, _password, _isLoading) { email, password, loading ->
        email.matches(emailRegex) && password.length >= 6 && !loading
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onEmailChange(newValue: String) {
        _email.value = newValue
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
    }

    fun onLoginClick() {
        if (isLoginEnabled.value) {
            _isLoading.value = true
            _error.value = null
            viewModelScope.launch {
                val result = authRepository.login(_email.value, _password.value)
                _isLoading.value = false
                result.onFailure { error ->
                    _error.value = error.message ?: "An unknown error occurred"
                }
            }
        }
    }

    fun onGoogleLoginSuccess(idToken: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(idToken)
            _isLoading.value = false
            result.onFailure { error ->
                _error.value = error.message ?: "Google sign-in failed"
            }
        }
    }

    fun onGoogleLoginError(message: String) {
        _error.value = message
    }

    fun onForgotPasswordClick() {
        if (_email.value.matches(emailRegex)) {
            _isLoading.value = true
            _error.value = null
            _message.value = null
            viewModelScope.launch {
                val result = authRepository.sendPasswordResetEmail(_email.value)
                _isLoading.value = false
                result.onSuccess {
                    _message.value = "Password reset email sent!"
                }
                result.onFailure { error ->
                    _error.value = error.message ?: "Failed to send reset email"
                }
            }
        } else {
            _error.value = "Please enter a valid email address first"
        }
    }

    fun clearMessages() {
        _error.value = null
        _message.value = null
    }
}
