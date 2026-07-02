package org.alphakids.app.data.remote

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Creates and configures the [HttpClient] for all API communication.
 *
 * Features:
 * - JSON serialization via kotlinx with lenient parsing
 * - 30s request timeout
 * - Content-Type: application/json by default
 */
fun createApiClient(): HttpClient {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    return HttpClient {
        install(ContentNegotiation) {
            json(json)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }

        defaultRequest {
            url(ApiConstants.BASE_URL)
            contentType(ContentType.Application.Json)
        }
    }
}

/**
 * Adds the Bearer token header from [tokenStorage] to this request.
 */
fun HttpRequestBuilder.authorize(tokenStorage: TokenStorage) {
    tokenStorage.accessToken?.let {
        header(HttpHeaders.Authorization, "Bearer $it")
    }
}
