package org.alphakids.app.data.fake

import org.alphakids.app.onboarding.domain.model.AuthResponse
import org.alphakids.app.onboarding.domain.model.LoginRequest
import org.alphakids.app.onboarding.domain.model.OTPGenerationResult
import org.alphakids.app.onboarding.domain.model.RegisterRequest
import org.alphakids.app.onboarding.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {

    override suspend fun login(request: LoginRequest): AuthResponse = AuthResponse(
        success = true,
        message = "Offline login",
        email = request.email,
        accessToken = "fake-access",
        refreshToken = "fake-refresh",
    )

    override suspend fun register(request: RegisterRequest): AuthResponse = AuthResponse(
        success = true,
        message = "Offline register",
        email = request.email,
        accessToken = "fake-access",
        refreshToken = "fake-refresh",
    )

    override suspend fun sendOtp(email: String): OTPGenerationResult =
        OTPGenerationResult(code = "000000", expiresAt = Long.MAX_VALUE)

    override suspend fun verifyOtp(email: String, code: String): Boolean =
        code.length == 6 && code.all { it.isDigit() }

    override suspend fun resendOtp(email: String): OTPGenerationResult =
        OTPGenerationResult(code = "000000", expiresAt = Long.MAX_VALUE)

    override suspend fun isLoggedIn(): Boolean = true

    override suspend fun logout() {}
}
