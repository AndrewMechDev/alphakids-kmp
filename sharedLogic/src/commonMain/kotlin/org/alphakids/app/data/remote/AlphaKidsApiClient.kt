package org.alphakids.app.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.alphakids.app.data.remote.dto.AuthResponseDto

/**
 * HTTP client for the AlphaKids API following the mobile integration guide.
 *
 * Features:
 * - Auto-injects Bearer token via [tokenProvider] in every request
 * - JSON serialization via kotlinx (lenient, ignores unknown keys)
 * - 30s request timeout
 * - Content-Type: application/json by default
 *
 * Usage (via Koin):
 * ```
 * val api = AlphaKidsApiClient(tokenStorage)
 * api.httpClient.get("/tutors/children")
 * ```
 */
class AlphaKidsApiClient(
    private val tokenStorage: TokenStorage,
    private val baseUrl: String = ApiConstants.BASE_URL,
) {
    val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = false
                isLenient = true
                ignoreUnknownKeys = true
                // Only encode non-default fields in requests so Zod's
                // .optional() (which rejects null) receives undefined instead.
                encodeDefaults = false
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }

        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)

            // Auto-inject Bearer token from storage
            tokenStorage.accessToken?.let {
                header(HttpHeaders.Authorization, "Bearer $it")
            }
        }
    }

    /**
     * Refreshes tokens by calling POST /auth/refresh.
     * Returns true if the refresh succeeded.
     */
    suspend fun refreshTokens(): Boolean {
        val refreshToken = tokenStorage.refreshToken ?: return false

        return try {
            val response = httpClient.post(ApiConstants.REFRESH) {
                setBody(mapOf("refresh_token" to refreshToken))
            }
            if (response.status.isSuccess()) {
                val body = response.body<AuthResponseDto>()
                tokenStorage.accessToken = body.accessToken
                tokenStorage.refreshToken = body.refreshToken
                true
            } else {
                tokenStorage.clear()
                false
            }
        } catch (_: Exception) {
            tokenStorage.clear()
            false
        }
    }
}
