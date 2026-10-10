package org.alphakids.app.data.remote

import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Behaviour of the bearer [io.ktor.client.plugins.auth.Auth] setup in
 * [AlphaKidsApiClient]: refresh on 401 with a rotating refresh token, and the
 * difference between a rejected refresh (real logout) and a network failure.
 */
class AlphaKidsApiClientAuthTest {

    private val protectedPath = "/${ApiConstants.TUTORS_CHILDREN}"
    private val refreshPath = "/${ApiConstants.REFRESH}"

    @Test
    fun `401 refreshes once and retries with the new access token`() = runTest {
        val storage = InMemoryTokenStorage(access = "a1", refresh = "r1")
        val recorded = RecordedRequests()
        var refreshTokenSeenByRetry: String? = null
        val api = mockApi(recorded, storage) { request ->
            when (request.url.encodedPath) {
                refreshPath -> respondJson(TOKENS_2)
                protectedPath -> when (request.headers[HttpHeaders.Authorization]) {
                    "Bearer a2" -> {
                        refreshTokenSeenByRetry = storage.refreshToken
                        respondJson("[]")
                    }
                    else -> respondJson(UNAUTHORIZED, HttpStatusCode.Unauthorized)
                }
                else -> error("Unexpected ${request.url}")
            }
        }

        val response = api.httpClient.get(ApiConstants.TUTORS_CHILDREN)

        assertEquals(HttpStatusCode.OK, response.status)
        val refresh = recorded.all.single { it.url.encodedPath == refreshPath }
        assertEquals("""{"refresh_token":"r1"}""", refresh.jsonBody)
        assertNull(refresh.headers[HttpHeaders.Authorization], "refresh must not carry the expired bearer")
        // The backend rotates the refresh token: the new one must be persisted
        // before the retried request runs, or the next refresh would send a
        // token the server already invalidated.
        assertEquals("r2", refreshTokenSeenByRetry)
        assertEquals("a2", storage.accessToken)
        assertEquals("r2", storage.refreshToken)
    }

    @Test
    fun `concurrent 401s share a single refresh`() = runTest {
        val storage = InMemoryTokenStorage(access = "a1", refresh = "r1")
        val lock = Mutex()
        var staleRequests = 0
        var refreshCalls = 0
        val bothStaleInFlight = CompletableDeferred<Unit>()
        val api = mockApi(tokenStorage = storage) { request ->
            when (request.url.encodedPath) {
                refreshPath -> {
                    lock.withLock { refreshCalls++ }
                    respondJson(TOKENS_2)
                }
                protectedPath -> when (request.headers[HttpHeaders.Authorization]) {
                    "Bearer a2" -> respondJson("[]")
                    else -> {
                        // Hold both stale requests until each one is in
                        // flight, so both 401s race into the refresh.
                        val inFlight = lock.withLock { ++staleRequests }
                        if (inFlight == 2) bothStaleInFlight.complete(Unit)
                        bothStaleInFlight.await()
                        respondJson(UNAUTHORIZED, HttpStatusCode.Unauthorized)
                    }
                }
                else -> error("Unexpected ${request.url}")
            }
        }

        val statuses = listOf(
            async { api.httpClient.get(ApiConstants.TUTORS_CHILDREN).status },
            async { api.httpClient.get(ApiConstants.TUTORS_CHILDREN).status },
        ).awaitAll()

        assertEquals(listOf(HttpStatusCode.OK, HttpStatusCode.OK), statuses)
        assertEquals(1, refreshCalls, "a rotated refresh token can only be used once")
        assertEquals("r2", storage.refreshToken)
    }

    @Test
    fun `rejected refresh clears the stored tokens`() = runTest {
        val storage = InMemoryTokenStorage(access = "a1", refresh = "r1")
        val api = mockApi(tokenStorage = storage) { request ->
            when (request.url.encodedPath) {
                refreshPath -> respondJson(REFRESH_REJECTED, HttpStatusCode.Unauthorized)
                else -> respondJson(UNAUTHORIZED, HttpStatusCode.Unauthorized)
            }
        }

        val response = api.httpClient.get(ApiConstants.TUTORS_CHILDREN)

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertNull(storage.accessToken)
        assertNull(storage.refreshToken)
    }

    @Test
    fun `network failure during refresh keeps the stored tokens`() = runTest {
        val storage = InMemoryTokenStorage(access = "a1", refresh = "r1")
        val api = mockApi(tokenStorage = storage) { request ->
            when (request.url.encodedPath) {
                refreshPath -> throw IOException("connection reset")
                else -> respondJson(UNAUTHORIZED, HttpStatusCode.Unauthorized)
            }
        }

        val response = api.httpClient.get(ApiConstants.TUTORS_CHILDREN)

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("a1", storage.accessToken)
        assertEquals("r1", storage.refreshToken)
    }

    private companion object {
        const val TOKENS_2 = """{"access_token":"a2","refresh_token":"r2"}"""
        const val UNAUTHORIZED =
            """{"statusCode":401,"message":"Unauthorized","timestamp":"t","path":"/tutors/children"}"""
        const val REFRESH_REJECTED =
            """{"statusCode":401,"message":"El refresh token es inválido o expiró. Iniciá sesión nuevamente.","timestamp":"t","path":"/auth/refresh"}"""
    }
}
