package com.company.app.ui.register

import com.company.app.shared.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RegisterState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isNewUser: Boolean = true,
)

class RegisterViewModel(private val authRepo: AuthRepository) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _state.value = RegisterState(error = "All fields are required")
            return
        }
        if (password != confirmPassword) {
            _state.value = RegisterState(error = "Passwords do not match")
            return
        }
        if (password.length < 6) {
            _state.value = RegisterState(error = "Password must be at least 6 characters")
            return
        }
        _state.value = RegisterState(isLoading = true)
        scope.launch {
            authRepo.register(email, password, name)
                .onSuccess { _state.value = RegisterState(isSuccess = true, isNewUser = true) }
                .onFailure { _state.value = RegisterState(error = it.message ?: "Registration failed") }
        }
    }

    fun googleSignIn(idToken: String) {
        _state.value = RegisterState(isLoading = true)
        scope.launch {
            authRepo.googleSignIn(idToken)
                .onSuccess { _state.value = RegisterState(isSuccess = true, isNewUser = it.isNewUser) }
                .onFailure { _state.value = RegisterState(error = it.message ?: "Google sign-in failed") }
        }
    }

    fun showError(message: String) {
        _state.value = _state.value.copy(isLoading = false, error = message)
    }
}
