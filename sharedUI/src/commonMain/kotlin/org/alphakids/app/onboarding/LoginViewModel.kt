package org.alphakids.app.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.alphakids.app.onboarding.domain.model.LoginRequest
import org.alphakids.app.onboarding.domain.repository.AuthRepository
import org.alphakids.app.parent.domain.repository.ParentRepository

/**
 * UI state for the login screen.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val hasChildren: Boolean = false,
)

/**
 * ViewModel for [LoginScreen].
 *
 * Handles email/password input, validation, and authentication through [AuthRepository].
 */
class LoginViewModel(
    private val authRepository: AuthRepository,
    private val parentRepository: ParentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun onLoginClick() {
        val state = _uiState.value

        // Validate email
        if (state.email.isBlank()) {
            _uiState.update { it.copy(error = "El email es requerido") }
            return
        }
        if (!isValidEmail(state.email)) {
            _uiState.update { it.copy(error = "Formato de email inválido") }
            return
        }

        // Validate password
        if (state.password.isBlank()) {
            _uiState.update { it.copy(error = "La contraseña es requerida") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }

        viewModelScope.launch {
            val response = authRepository.login(
                LoginRequest(email = state.email, password = state.password),
            )

            if (response.success) {
                val children = parentRepository.getChildren()
                _uiState.update {
                    it.copy(isLoading = false, isSuccess = true, hasChildren = children.isNotEmpty())
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = response.message) }
            }
        }
    }

    fun onRegisterClick() {
        _uiState.update { it.copy(isSuccess = true) }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
}
