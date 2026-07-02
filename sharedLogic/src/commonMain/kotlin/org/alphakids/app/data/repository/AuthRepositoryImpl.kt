package org.alphakids.app.data.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.alphakids.app.data.remote.ApiConstants
import org.alphakids.app.data.remote.TokenStorage
import org.alphakids.app.data.remote.authorize
import org.alphakids.app.data.remote.doRefreshTokens
import org.alphakids.app.data.remote.dto.*
import org.alphakids.app.onboarding.domain.model.AuthResponse
import org.alphakids.app.onboarding.domain.model.LoginRequest
import org.alphakids.app.onboarding.domain.model.OTPGenerationResult
import org.alphakids.app.onboarding.domain.model.RegisterRequest
import org.alphakids.app.onboarding.domain.repository.AuthRepository

/**
 * Real implementation of [AuthRepository] that calls the AlphaKids API.
 *
 * Handles token lifecycle:
 * - Login/Register → stores tokens via [TokenStorage]
 * - 401 on any request → attempts refresh via [doRefreshTokens]
 * - Refresh failure → clears tokens
 * - Logout → invalidates session + clears tokens
 */
class AuthRepositoryImpl(
    private val client: HttpClient,
    private val tokenStorage: TokenStorage,
) : AuthRepository {

    override suspend fun login(request: LoginRequest): AuthResponse {
        return try {
            val response = client.post(ApiConstants.LOGIN) {
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
            val response = client.post(ApiConstants.REGISTER) {
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

    /**
     * OTP methods — the API does not require OTP (register returns tokens directly).
     * These exist to support the existing UI flow without modifying screens.
     * [sendOtp] stores a dummy code; [verifyOtp] accepts any 6-digit code.
     */
    private var dummyOtpCode: String? = null

    override suspend fun sendOtp(email: String): OTPGenerationResult {
        // Dummy — the API doesn't require OTP; register returns tokens directly.
        // Any 6-digit code entered by the user will pass verification.
        dummyOtpCode = "000000"
        return OTPGenerationResult(
            code = dummyOtpCode!!,
            expiresAt = Long.MAX_VALUE,
        )
    }

    override suspend fun verifyOtp(email: String, code: String): Boolean {
        return code.length == 6 && code.all { it.isDigit() }
    }

    override suspend fun resendOtp(email: String): OTPGenerationResult {
        dummyOtpCode = "000001"
        return OTPGenerationResult(
            code = dummyOtpCode!!,
            expiresAt = Long.MAX_VALUE,
        )
    }

    override suspend fun isLoggedIn(): Boolean = tokenStorage.isLoggedIn

    override suspend fun logout() {
        try {
            tokenStorage.refreshToken?.let { refreshToken ->
                client.post(ApiConstants.LOGOUT) {
                    setBody(mapOf("refresh_token" to refreshToken))
                }
            }
        } catch (_: Exception) {
            // Best-effort logout — clear tokens regardless
        } finally {
            tokenStorage.clear()
        }
    }

    // ── Helpers ──

    /**
     * Attempts to refresh the access token using the stored refresh token.
     * Returns true if refresh succeeded, false otherwise.
     */
    suspend fun refreshSession(): Boolean {
        return doRefreshTokens(client, tokenStorage)
    }

    /**
     * Creates an [HttpRequestBuilder] with the Bearer token attached.
     * If the token is expired, this will still send the request;
     * the caller should handle 401 by calling [refreshSession] and retrying.
     */
    suspend fun prepareAuthenticatedRequest(): HttpRequestBuilder {
        val builder = HttpRequestBuilder().apply {
            authorize(tokenStorage)
        }
        return builder
    }

    /**
     * Reads an error message from the API response body.
     * Falls back to the status code description.
     */
    private suspend fun parseErrorMessage(response: io.ktor.client.statement.HttpResponse): String {
        return try {
            val body = response.body<Map<String, String>>()
            body["message"] ?: body["error"] ?: "Error ${response.status.value}"
        } catch (_: Exception) {
            "Error ${response.status.value}"
        }
    }
}
