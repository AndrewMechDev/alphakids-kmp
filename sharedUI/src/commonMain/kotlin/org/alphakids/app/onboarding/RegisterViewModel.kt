package org.alphakids.app.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.alphakids.app.onboarding.domain.model.RegisterRequest
import org.alphakids.app.onboarding.domain.repository.AuthRepository

/**
 * UI state for the registration screen.
 */
data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
) {
    /** Returns true if all required fields are filled and terms are accepted. */
    val isFormValid: Boolean
        get() = name.isNotBlank()
                && email.isNotBlank()
                && phone.isNotBlank()
                && password.length >= 6
                && confirmPassword == password
                && termsAccepted
}

/**
 * ViewModel for [RegisterScreen].
 *
 * Handles registration form input, validation, and account creation through [AuthRepository].
 */
class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, error = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPhoneChanged(value: String) {
        _uiState.update { it.copy(phone = value, error = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmPassword = value, error = null) }
    }

    fun onTermsChanged(accepted: Boolean) {
        _uiState.update { it.copy(termsAccepted = accepted, error = null) }
    }

    fun onRegisterClick() {
        val state = _uiState.value

        // Validate name
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es requerido") }
            return
        }

        // Validate email
        if (state.email.isBlank()) {
            _uiState.update { it.copy(error = "El email es requerido") }
            return
        }
        if (!isValidEmail(state.email)) {
            _uiState.update { it.copy(error = "Formato de email inválido") }
            return
        }

        // Validate phone
        if (state.phone.isBlank()) {
            _uiState.update { it.copy(error = "El teléfono es requerido") }
            return
        }
        if (state.phone.length < 8) {
            _uiState.update { it.copy(error = "El teléfono debe tener al menos 8 dígitos") }
            return
        }

        // Validate password
        if (state.password.length < 6) {
            _uiState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
            return
        }

        // Validate password match
        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(error = "Las contraseñas no coinciden") }
            return
        }

        // Validate terms
        if (!state.termsAccepted) {
            _uiState.update { it.copy(error = "Debes aceptar los términos y condiciones") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val response = authRepository.register(
                RegisterRequest(
                    name = state.name,
                    email = state.email,
                    phone = state.phone,
                    password = state.password,
                ),
            )

            if (response.success) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = response.message) }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
}
