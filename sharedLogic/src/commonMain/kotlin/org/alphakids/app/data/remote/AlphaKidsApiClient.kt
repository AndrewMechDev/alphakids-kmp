package org.alphakids.app.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
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
 * - Bearer token auto-injected and auto-refreshed on 401 via Ktor's [Auth]
 *   plugin — every call site (including future repositories) is covered
 *   without reimplementing a manual "if 401 then refresh" block. Two real
 *   repositories (Store, StudentPet) shipped without that manual block
 *   before this was centralized, which meant purchases/pet-feeding just
 *   silently stopped working once the access token expired mid-session.
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

        install(Auth) {
            bearer {
                loadTokens {
                    val access = tokenStorage.accessToken ?: return@loadTokens null
                    BearerTokens(access, tokenStorage.refreshToken ?: "")
                }

                refreshTokens {
                    val refreshToken = tokenStorage.refreshToken ?: return@refreshTokens null
                    try {
                        val response = client.post(ApiConstants.REFRESH) {
                            markAsRefreshTokenRequest()
                            setBody(mapOf("refresh_token" to refreshToken))
                        }
                        if (!response.status.isSuccess()) {
                            // The server itself rejected the refresh token —
                            // it's genuinely invalid/expired. Log out for real.
                            tokenStorage.clear()
                            return@refreshTokens null
                        }
                        val body = response.body<AuthResponseDto>()
                        tokenStorage.accessToken = body.accessToken
                        tokenStorage.refreshToken = body.refreshToken
                        BearerTokens(body.accessToken, body.refreshToken)
                    } catch (_: Exception) {
                        // A network failure while refreshing is NOT the same
                        // as an invalid refresh token — don't clear it, or a
                        // brief connectivity blip logs the child out for no
                        // real reason. The caller's own request just fails
                        // this once and can be retried later.
                        null
                    }
                }
            }
        }

        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
    }
}
