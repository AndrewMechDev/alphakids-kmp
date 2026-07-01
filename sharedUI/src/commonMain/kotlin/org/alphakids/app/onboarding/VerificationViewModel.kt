package org.alphakids.app.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.alphakids.app.onboarding.domain.repository.AuthRepository

/**
 * UI state for the OTP verification screen.
 *
 * The [code] field holds a 6-character string where spaces represent
 * unfilled positions (e.g., "12     " means digits 1,2 are filled).
 */
data class VerificationUiState(
    val email: String = "",
    val code: String = "      ", // 6 chars: spaces for unfilled positions
    val isLoading: Boolean = false,
    val error: String? = null,
    val isVerified: Boolean = false,
    val resendCooldown: Int = 0,
    val canResend: Boolean = false,
)

private const val OTP_LENGTH = 6
private const val RESEND_COOLDOWN_SECONDS = 30

/**
 * ViewModel for [VerificationScreen].
 *
 * Handles OTP digit input, verification, resend with cooldown timer.
 * Requires [email] to be provided, typically passed from navigation arguments.
 */
class VerificationViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    private var cooldownJob: Job? = null

    /**
     * Initialize with the email address and trigger OTP generation.
     * Should be called once when the screen is created.
     */
    fun initialize(email: String) {
        _uiState.update { it.copy(email = email, code = "      ") }
        sendOtp()
    }

    private fun sendOtp() {
        viewModelScope.launch {
            authRepository.sendOtp(_uiState.value.email)
            // In mock, the result contains the code for debugging; no UI update needed
        }
    }

    /**
     * Called when the OTP code changes (from OTPInputField).
     * Pads the incoming code to 6 characters with spaces as needed.
     */
    fun onCodeChanged(newCode: String) {
        val padded = newCode.padEnd(OTP_LENGTH, ' ').take(OTP_LENGTH)
        _uiState.update { it.copy(code = padded, error = null) }
    }

    fun onVerifyClick() {
        val code = _uiState.value.code
        if (code.any { it == ' ' }) {
            _uiState.update { it.copy(error = "Ingresa el código completo de 6 dígitos") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val verified = authRepository.verifyOtp(
                email = _uiState.value.email,
                code = code,
            )

            if (verified) {
                _uiState.update { it.copy(isLoading = false, isVerified = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Código incorrecto") }
            }
        }
    }

    fun onResendClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.resendOtp(_uiState.value.email)
            _uiState.update { it.copy(isLoading = false) }
            startResendCooldown()
        }
    }

    private fun startResendCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            _uiState.update { it.copy(resendCooldown = RESEND_COOLDOWN_SECONDS, canResend = false) }
            for (i in RESEND_COOLDOWN_SECONDS downTo 1) {
                delay(1000L)
                _uiState.update { it.copy(resendCooldown = i - 1) }
            }
            _uiState.update { it.copy(resendCooldown = 0, canResend = true) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cooldownJob?.cancel()
    }
}
