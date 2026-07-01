package org.alphakids.app.onboarding.domain.model

/**
 * Value object for a 6-digit OTP code.
 *
 * @throws IllegalArgumentException if the value is not exactly 6 digits.
 */
data class OTPCode(val value: String) {
    init {
        require(value.length == 6 && value.all { it.isDigit() }) {
            "OTP code must be exactly 6 digits"
        }
    }
}

/**
 * Result of an OTP generation operation.
 */
data class OTPGenerationResult(
    val code: String,
    val expiresAt: Long, // epoch millis
)
