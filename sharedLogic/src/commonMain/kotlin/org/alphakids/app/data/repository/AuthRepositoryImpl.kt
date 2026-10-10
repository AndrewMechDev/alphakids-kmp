package org.alphakids.app.data.repository

import io.ktor.client.call.*
import io.ktor.client.plugins.timeout
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.remote.ApiConstants
import org.alphakids.app.data.remote.TokenStorage
import org.alphakids.app.data.remote.dto.*
import org.alphakids.app.data.remote.withoutBearerAuth
import org.alphakids.app.onboarding.domain.model.AuthResponse
import org.alphakids.app.onboarding.domain.model.LoginRequest
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
 * - Token refresh is handled by the client's bearer Auth plugin
 * - Failures carry friendly Spanish messages from [AuthErrorMessages]
 */
class AuthRepositoryImpl(
    private val api: AlphaKidsApiClient,
    private val tokenStorage: TokenStorage,
) : AuthRepository {

    override suspend fun login(request: LoginRequest): AuthResponse = authenticate(
        operation = AuthOperation.LOGIN,
        path = ApiConstants.LOGIN,
        body = LoginRequestDto(email = request.email, password = request.password),
        email = request.email,
        successMessage = "Inicio de sesión exitoso",
    )

    override suspend fun register(request: RegisterRequest): AuthResponse = authenticate(
        operation = AuthOperation.REGISTER,
        path = ApiConstants.REGISTER,
        body = RegisterRequestDto(email = request.email, password = request.password, name = request.name),
        email = request.email,
        successMessage = "Registro exitoso",
    )

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

    /**
     * Posts credentials to [path] and stores the returned token pair. Every
     * failure becomes an [AuthResponse] with a message from
     * [AuthErrorMessages]; cancellation is never swallowed.
     */
    private suspend inline fun <reified T : Any> authenticate(
        operation: AuthOperation,
        path: String,
        body: T,
        email: String,
        successMessage: String,
    ): AuthResponse {
        val response = try {
            api.httpClient.post(path) {
                withoutBearerAuth()
                setBody(body)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return failure(email, AuthErrorMessages.forFailure(e))
        }

        if (!response.status.isSuccess()) {
            return failure(
                email,
                AuthErrorMessages.forStatus(operation, response.status.value, response.backendMessage()),
            )
        }

        val tokens = try {
            response.body<AuthResponseDto>()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            return failure(email, AuthErrorMessages.UNEXPECTED)
        }
        storeTokens(tokens)
        return AuthResponse(
            success = true,
            message = successMessage,
            email = email,
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )
    }

    private fun failure(email: String, message: String) =
        AuthResponse(success = false, message = message, email = email)

    /** The `message` of the backend's `{statusCode, message}` error body, if readable. */
    private suspend fun HttpResponse.backendMessage(): String? = try {
        body<ApiErrorDto>().message
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    private companion object {
        /** Logout must not keep the user waiting on a slow revocation. */
        const val LOGOUT_TIMEOUT_MS = 5_000L
    }
}
