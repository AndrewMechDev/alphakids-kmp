package org.alphakids.app.onboarding.domain.repository

import org.alphakids.app.onboarding.domain.model.AuthResponse
import org.alphakids.app.onboarding.domain.model.LoginRequest
import org.alphakids.app.onboarding.domain.model.OTPGenerationResult
import org.alphakids.app.onboarding.domain.model.RegisterRequest

/**
 * Repository interface for parent authentication and OTP verification.
 */
interface AuthRepository {

    /** Authenticate a parent with email and password. */
    suspend fun login(request: LoginRequest): AuthResponse

    /** Register a new parent account. */
    suspend fun register(request: RegisterRequest): AuthResponse

    /** Generate and send an OTP code to the given email. Returns the code and expiry. */
    suspend fun sendOtp(email: String): OTPGenerationResult

    /** Verify an OTP code for the given email. Returns true if code is valid. */
    suspend fun verifyOtp(email: String, code: String): Boolean

    /** Resend a new OTP code. Returns the new code and expiry. */
    suspend fun resendOtp(email: String): OTPGenerationResult

    /** Check if a user is currently logged in. */
    suspend fun isLoggedIn(): Boolean
}
