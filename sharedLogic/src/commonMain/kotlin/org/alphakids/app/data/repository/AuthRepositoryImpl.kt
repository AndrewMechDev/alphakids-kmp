package org.alphakids.app.data.repository

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.remote.ApiConstants
import org.alphakids.app.data.remote.TokenStorage
import org.alphakids.app.data.remote.dto.*
import org.alphakids.app.onboarding.domain.model.AuthResponse
import org.alphakids.app.onboarding.domain.model.LoginRequest
import org.alphakids.app.onboarding.domain.model.OTPGenerationResult
import org.alphakids.app.onboarding.domain.model.RegisterRequest
import org.alphakids.app.onboarding.domain.repository.AuthRepository

/**
 * Real implementation of [AuthRepository] using [AlphaKidsApiClient].
 *
 * Token lifecycle:
 * - Login/Register → stores tokens via [TokenStorage]
 * - Logout → POST /auth/logout + clears tokens
 * - Token refresh delegated to [AlphaKidsApiClient.refreshTokens]
 */
class AuthRepositoryImpl(
    private val api: AlphaKidsApiClient,
    private val tokenStorage: TokenStorage,
) : AuthRepository {

    override suspend fun login(request: LoginRequest): AuthResponse {
        return try {
            val response = api.httpClient.post(ApiConstants.LOGIN) {
                setBody(LoginRequestDto(email = request.email, password = request.password))
            }

            if (response.status.isSuccess()) {
                val body = response.body<AuthResponseDto>()
                tokenStorage.accessToken = body.accessToken
                tokenStorage.refreshToken = body.refreshToken
                AuthResponse(
                    success = true,
                    message = "Inicio de sesión exitoso",
                    email = request.email,
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                )
            } else {
                val errorMsg = parseErrorMessage(response)
                AuthResponse(success = false, message = errorMsg, email = request.email)
            }
        } catch (e: Exception) {
            AuthResponse(
                success = false,
                message = "Error de conexión: ${e.message ?: "Intente de nuevo"}",
                email = request.email,
            )
        }
    }

    override suspend fun register(request: RegisterRequest): AuthResponse {
        return try {
            val response = api.httpClient.post(ApiConstants.REGISTER) {
                setBody(
                    RegisterRequestDto(
                        email = request.email,
                        password = request.password,
                        name = request.name,
                    ),
                )
            }

            if (response.status.isSuccess()) {
                val body = response.body<AuthResponseDto>()
                tokenStorage.accessToken = body.accessToken
                tokenStorage.refreshToken = body.refreshToken
                AuthResponse(
                    success = true,
                    message = "Registro exitoso",
                    email = request.email,
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                )
            } else {
                val errorMsg = parseErrorMessage(response)
                AuthResponse(success = false, message = errorMsg, email = request.email)
            }
        } catch (e: Exception) {
            AuthResponse(
                success = false,
                message = "Error de conexión: ${e.message ?: "Intente de nuevo"}",
                email = request.email,
            )
        }
    }

    // ── OTP (dummy — register returns tokens directly) ──

    private var dummyOtpCode: String? = null

    override suspend fun sendOtp(email: String): OTPGenerationResult {
        dummyOtpCode = "000000"
        return OTPGenerationResult(code = dummyOtpCode!!, expiresAt = Long.MAX_VALUE)
    }

    override suspend fun verifyOtp(email: String, code: String): Boolean {
        return code.length == 6 && code.all { it.isDigit() }
    }

    override suspend fun resendOtp(email: String): OTPGenerationResult {
        dummyOtpCode = "000001"
        return OTPGenerationResult(code = dummyOtpCode!!, expiresAt = Long.MAX_VALUE)
    }

    override suspend fun isLoggedIn(): Boolean = tokenStorage.isLoggedIn

    override suspend fun logout() {
        try {
            tokenStorage.refreshToken?.let { refreshToken ->
                api.httpClient.post(ApiConstants.LOGOUT) {
                    setBody(mapOf("refresh_token" to refreshToken))
                }
            }
        } catch (_: Exception) {
            // Best-effort
        } finally {
            tokenStorage.clear()
        }
    }

    // ── Helpers ──

    private suspend fun parseErrorMessage(response: io.ktor.client.statement.HttpResponse): String {
        return try {
            val body = response.body<Map<String, String>>()
            body["message"] ?: body["error"] ?: "Error ${response.status.value}"
        } catch (_: Exception) {
            "Error ${response.status.value}"
        }
    }
}
