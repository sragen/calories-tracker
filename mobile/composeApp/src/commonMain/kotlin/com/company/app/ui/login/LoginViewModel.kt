package com.company.app.ui.login

import com.company.app.shared.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isNewUser: Boolean = false,
)

class LoginViewModel(private val authRepo: AuthRepository) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = LoginState(error = "Email and password are required")
            return
        }
        _state.value = LoginState(isLoading = true)
        scope.launch {
            authRepo.login(email, password)
                .onSuccess { _state.value = LoginState(isSuccess = true) }
                .onFailure { _state.value = LoginState(error = it.message ?: "Login failed") }
        }
    }

    fun googleSignIn(idToken: String) {
        _state.value = LoginState(isLoading = true)
        scope.launch {
            authRepo.googleSignIn(idToken)
                .onSuccess { _state.value = LoginState(isSuccess = true, isNewUser = it.isNewUser) }
                .onFailure { _state.value = LoginState(error = it.message ?: "Google sign-in failed") }
        }
    }

    fun showError(message: String) {
        _state.value = _state.value.copy(isLoading = false, error = message)
    }
}
