package org.alphakids.app.data.repository

import io.ktor.client.call.*
import io.ktor.client.plugins.timeout
import io.ktor.client.request.*
import io.ktor.http.*
import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.remote.ApiConstants
import org.alphakids.app.data.remote.TokenStorage
import org.alphakids.app.data.remote.dto.*
import org.alphakids.app.data.remote.withoutBearerAuth
import org.alphakids.app.onboarding.domain.model.AuthResponse
import org.alphakids.app.onboarding.domain.model.LoginRequest
import org.alphakids.app.onboarding.domain.model.OTPGenerationResult
import org.alphakids.app.onboarding.domain.model.RegisterRequest
import org.alphakids.app.onboarding.domain.repository.AuthRepository
import kotlin.coroutines.cancellation.CancellationException

/**
 * Real implementation of [AuthRepository] using [AlphaKidsApiClient].
 *
 * Token lifecycle:
 * - Login/Register → stores tokens via [TokenStorage]
 * - Logout → clears tokens, then revokes the refresh token (POST /auth/logout)
 * - Every change to the stored tokens also resets the client's cached bearer
 * - Token refresh delegated to [AlphaKidsApiClient.refreshTokens]
 */
class AuthRepositoryImpl(
    private val api: AlphaKidsApiClient,
    private val tokenStorage: TokenStorage,
) : AuthRepository {

    override suspend fun login(request: LoginRequest): AuthResponse {
        return try {
            val response = api.httpClient.post(ApiConstants.LOGIN) {
                withoutBearerAuth()
                setBody(LoginRequestDto(email = request.email, password = request.password))
            }

            if (response.status.isSuccess()) {
                val body = response.body<AuthResponseDto>()
                storeTokens(body)
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
                withoutBearerAuth()
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
                storeTokens(body)
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

    /**
     * Local sign-out first, server revocation second: the stored tokens and
     * the client's cached bearer are gone before the network call starts, so
     * a cancelled or failed revocation can never leave the session usable.
     */
    override suspend fun logout() {
        val refreshToken = tokenStorage.refreshToken
        tokenStorage.clear()
        api.invalidateCachedTokens()
        if (refreshToken == null) return
        try {
            api.httpClient.post(ApiConstants.LOGOUT) {
                withoutBearerAuth()
                timeout { requestTimeoutMillis = LOGOUT_TIMEOUT_MS }
                setBody(RefreshRequestDto(refreshToken))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Best-effort: the refresh token expires on its own in 7 days.
        }
    }

    // ── Helpers ──

    /** Persists a fresh token pair and makes the client pick it up. */
    private fun storeTokens(tokens: AuthResponseDto) {
        tokenStorage.accessToken = tokens.accessToken
        tokenStorage.refreshToken = tokens.refreshToken
        api.invalidateCachedTokens()
    }

    private suspend fun parseErrorMessage(response: io.ktor.client.statement.HttpResponse): String {
        return try {
            val body = response.body<Map<String, String>>()
            body["message"] ?: body["error"] ?: "Error ${response.status.value}"
        } catch (_: Exception) {
            "Error ${response.status.value}"
        }
    }

    private companion object {
        /** Logout must not keep the user waiting on a slow revocation. */
        const val LOGOUT_TIMEOUT_MS = 5_000L
    }
}
