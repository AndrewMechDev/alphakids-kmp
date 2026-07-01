package org.alphakids.app.onboarding.data.mock

import org.alphakids.app.onboarding.domain.model.AuthResponse
import org.alphakids.app.onboarding.domain.model.LoginRequest
import org.alphakids.app.onboarding.domain.model.OTPGenerationResult
import org.alphakids.app.onboarding.domain.model.RegisterRequest
import org.alphakids.app.onboarding.domain.model.ValidationResult
import org.alphakids.app.onboarding.domain.repository.AuthRepository
import kotlin.random.Random

/**
 * In-memory mock implementation of [AuthRepository].
 *
 * Stores registered users in a mutable map and simulates OTP generation/verification
 * with a cooldown timer and attempt limit.
 */
class MockAuthRepository : AuthRepository {

    // In-memory user store: email → hashed password
    private val users = mutableMapOf<String, UserRecord>()

    // OTP store: email → OtpRecord
    private val otpStore = mutableMapOf<String, OtpRecord>()

    // Login session flag
    private var loggedIn = false

    override suspend fun login(request: LoginRequest): AuthResponse {
        // Validate email format
        val emailValidation = validateEmail(request.email)
        if (!emailValidation.isValid) {
            return AuthResponse(
                success = false,
                message = emailValidation.errorMessage ?: "Email inválido",
                email = request.email,
            )
        }

        // Validate password not empty
        if (request.password.isBlank()) {
            return AuthResponse(
                success = false,
                message = "La contraseña no puede estar vacía",
                email = request.email,
            )
        }

        // Check credentials
        val record = users[request.email]
        return if (record != null && record.password == request.password) {
            loggedIn = true
            AuthResponse(
                success = true,
                message = "Inicio de sesión exitoso",
                email = request.email,
            )
        } else {
            AuthResponse(
                success = false,
                message = if (record == null) "Email no registrado" else "Contraseña incorrecta",
                email = request.email,
            )
        }
    }

    override suspend fun register(request: RegisterRequest): AuthResponse {
        // Validate name
        if (request.name.isBlank()) {
            return AuthResponse(
                success = false,
                message = "El nombre es requerido",
                email = request.email,
            )
        }

        // Validate email
        val emailValidation = validateEmail(request.email)
        if (!emailValidation.isValid) {
            return AuthResponse(
                success = false,
                message = emailValidation.errorMessage ?: "Email inválido",
                email = request.email,
            )
        }

        // Validate phone
        val phoneValidation = validatePhone(request.phone)
        if (!phoneValidation.isValid) {
            return AuthResponse(
                success = false,
                message = phoneValidation.errorMessage ?: "Teléfono inválido",
                email = request.email,
            )
        }

        // Validate password strength
        if (request.password.length < 6) {
            return AuthResponse(
                success = false,
                message = "La contraseña debe tener al menos 6 caracteres",
                email = request.email,
            )
        }

        // Check duplicate
        if (users.containsKey(request.email)) {
            return AuthResponse(
                success = false,
                message = "Este email ya está registrado",
                email = request.email,
            )
        }

        // Store user
        users[request.email] = UserRecord(
            name = request.name,
            email = request.email,
            phone = request.phone,
            password = request.password,
        )

        return AuthResponse(
            success = true,
            message = "Registro exitoso",
            email = request.email,
        )
    }

    override suspend fun sendOtp(email: String): OTPGenerationResult {
        val code = generateOtpCode()
        val now = tickCount()
        val expiresAt = now + OTP_EXPIRY_TICKS
        otpStore[email] = OtpRecord(
            code = code,
            createdAt = now,
            expiresAt = expiresAt,
            attempts = 0,
        )
        return OTPGenerationResult(code = code, expiresAt = expiresAt)
    }

    override suspend fun verifyOtp(email: String, code: String): Boolean {
        val record = otpStore[email] ?: return false
        val now = tickCount()

        // Check expiry
        if (now > record.expiresAt) {
            return false
        }

        // Check attempt limit
        if (record.attempts >= MAX_OTP_ATTEMPTS) {
            return false
        }

        // Increment attempts
        otpStore[email] = record.copy(attempts = record.attempts + 1)

        return record.code == code
    }

    override suspend fun resendOtp(email: String): OTPGenerationResult {
        val now = tickCount()
        val existing = otpStore[email]

        // Cooldown check: must wait COOLDOWN_TICKS between resends
        if (existing != null && (now - existing.createdAt) < OTP_COOLDOWN_TICKS) {
            // Still in cooldown — return existing code
            return OTPGenerationResult(code = existing.code, expiresAt = existing.expiresAt)
        }

        // Generate new code
        val code = generateOtpCode()
        val expiresAt = now + OTP_EXPIRY_TICKS
        otpStore[email] = OtpRecord(
            code = code,
            createdAt = now,
            expiresAt = expiresAt,
            attempts = 0,
        )
        return OTPGenerationResult(code = code, expiresAt = expiresAt)
    }

    override suspend fun isLoggedIn(): Boolean = loggedIn

    override suspend fun logout() {
        loggedIn = false
    }

    // ── Helpers ──

    private fun generateOtpCode(): String {
        return (1..6).map { Random.nextInt(0, 10).toString() }.joinToString("")
    }

    /**
     * Simple monotonic tick counter for relative time (no kotlinx-datetime dependency).
     * Used only for relative comparisons (expiry, cooldown), not absolute time.
     */
    private var counter = 0L
    private fun tickCount(): Long {
        counter++
        return counter
    }

    private fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) return ValidationResult.invalid("El email es requerido")
        if (!email.contains("@") || !email.contains(".")) {
            return ValidationResult.invalid("Formato de email inválido")
        }
        return ValidationResult.valid()
    }

    private fun validatePhone(phone: String): ValidationResult {
        if (phone.isBlank()) return ValidationResult.invalid("El teléfono es requerido")
        if (phone.length < 8) return ValidationResult.invalid("El teléfono debe tener al menos 8 dígitos")
        if (!phone.all { it.isDigit() || it == '+' || it == '-' || it == ' ' }) {
            return ValidationResult.invalid("Formato de teléfono inválido")
        }
        return ValidationResult.valid()
    }

    // ── Data Classes ──

    private data class UserRecord(
        val name: String,
        val email: String,
        val phone: String,
        val password: String,
    )

    private data class OtpRecord(
        val code: String,
        val createdAt: Long,
        val expiresAt: Long,
        val attempts: Int,
    )

    companion object {
        /** Expiry in relative ticks (~5 minutes of operations). */
        const val OTP_EXPIRY_TICKS = 300L

        /** Cooldown in relative ticks (~30 seconds). */
        const val OTP_COOLDOWN_TICKS = 30L

        /** Maximum OTP verification attempts before requiring resend. */
        const val MAX_OTP_ATTEMPTS = 3

        /** Demo credentials for testing. */
        val DEMO_EMAIL = "test@alphakids.com"
        val DEMO_PASSWORD = "123456"

        /**
         * Creates a [MockAuthRepository] pre-populated with demo credentials.
         */
        fun createWithDemoUser(): MockAuthRepository {
            return MockAuthRepository().apply {
                users[DEMO_EMAIL] = UserRecord(
                    name = "Test Parent",
                    email = DEMO_EMAIL,
                    phone = "123456789",
                    password = DEMO_PASSWORD,
                )
            }
        }
    }
}
