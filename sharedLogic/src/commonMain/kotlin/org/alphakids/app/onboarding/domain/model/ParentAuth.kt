package org.alphakids.app.onboarding.domain.model

/**
 * Request payload for parent login.
 */
data class LoginRequest(
    val email: String,
    val password: String,
)

/**
 * Request payload for parent registration.
 */
data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
)

/**
 * Response from auth operations.
 */
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val email: String,
)

/**
 * Generic validation result for form fields.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
) {
    companion object {
        fun valid() = ValidationResult(isValid = true)
        fun invalid(message: String) = ValidationResult(isValid = false, errorMessage = message)
    }
}
