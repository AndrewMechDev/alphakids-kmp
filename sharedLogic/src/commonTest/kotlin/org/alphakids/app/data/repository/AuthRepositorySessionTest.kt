package org.alphakids.app.data.repository

import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import org.alphakids.app.data.remote.ApiConstants
import org.alphakids.app.data.remote.InMemoryTokenStorage
import org.alphakids.app.data.remote.RecordedRequests
import org.alphakids.app.data.remote.jsonBody
import org.alphakids.app.data.remote.mockApi
import org.alphakids.app.data.remote.respondJson
import org.alphakids.app.onboarding.domain.model.LoginRequest
import org.alphakids.app.onboarding.domain.model.RegisterRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Token lifecycle of [AuthRepositoryImpl] against the real client setup:
 * what is stored, what is revoked, and which bearer the next request sends.
 */
class AuthRepositorySessionTest {

    private val loginPath = "/${ApiConstants.LOGIN}"
    private val registerPath = "/${ApiConstants.REGISTER}"
    private val logoutPath = "/${ApiConstants.LOGOUT}"
    private val protectedPath = "/${ApiConstants.TUTORS_CHILDREN}"

    @Test
    fun `login stores both tokens without sending a bearer`() = runTest {
        val storage = InMemoryTokenStorage()
        val recorded = RecordedRequests()
        val repo = AuthRepositoryImpl(
            mockApi(recorded, storage) { respondJson(tokens("a1", "r1")) },
            storage,
        )

        val response = repo.login(LoginRequest("ana@correo.com", "secreto123"))

        assertTrue(response.success)
        assertEquals("a1", storage.accessToken)
        assertEquals("r1", storage.refreshToken)
        assertEquals(loginPath, recorded.last.url.encodedPath)
        assertEquals("""{"email":"ana@correo.com","password":"secreto123"}""", recorded.last.jsonBody)
        assertNull(recorded.last.headers[HttpHeaders.Authorization])
    }

    @Test
    fun `register stores both tokens`() = runTest {
        val storage = InMemoryTokenStorage()
        val repo = AuthRepositoryImpl(mockApi(tokenStorage = storage) { respondJson(tokens("a1", "r1"), HttpStatusCode.Created) }, storage)

        val response = repo.register(RegisterRequest("Ana", "ana@correo.com", "999999999", "secreto123"))

        assertTrue(response.success)
        assertEquals("a1", storage.accessToken)
        assertEquals("r1", storage.refreshToken)
    }

    @Test
    fun `after logout the next login sends the new parent's token`() = runTest {
        // Regression: the Auth plugin cached the first parent's bearer and kept
        // sending it after logout + login, until a 401 forced a refresh.
        val storage = InMemoryTokenStorage(access = "parent-1", refresh = "refresh-1")
        val recorded = RecordedRequests()
        val api = mockApi(recorded, storage) { request ->
            when (request.url.encodedPath) {
                logoutPath -> respondJson("""{"message":"Sesión cerrada exitosamente"}""")
                loginPath -> respondJson(tokens("parent-2", "refresh-2"))
                protectedPath -> respondJson("[]")
                else -> error("Unexpected ${request.url}")
            }
        }
        val repo = AuthRepositoryImpl(api, storage)

        api.httpClient.get(ApiConstants.TUTORS_CHILDREN)
        repo.logout()
        repo.login(LoginRequest("otro@correo.com", "secreto123"))
        api.httpClient.get(ApiConstants.TUTORS_CHILDREN)

        val bearers = recorded.all
            .filter { it.url.encodedPath == protectedPath }
            .map { it.headers[HttpHeaders.Authorization] }
        assertEquals(listOf("Bearer parent-1", "Bearer parent-2"), bearers)
    }

    @Test
    fun `login replaces a bearer cached from an earlier session`() = runTest {
        val storage = InMemoryTokenStorage(access = "stale", refresh = "stale-refresh")
        val recorded = RecordedRequests()
        val api = mockApi(recorded, storage) { request ->
            when (request.url.encodedPath) {
                loginPath -> respondJson(tokens("fresh", "fresh-refresh"))
                protectedPath -> respondJson("[]")
                else -> error("Unexpected ${request.url}")
            }
        }
        val repo = AuthRepositoryImpl(api, storage)

        api.httpClient.get(ApiConstants.TUTORS_CHILDREN)
        repo.login(LoginRequest("ana@correo.com", "secreto123"))
        api.httpClient.get(ApiConstants.TUTORS_CHILDREN)

        assertEquals("Bearer fresh", recorded.last.headers[HttpHeaders.Authorization])
    }

    @Test
    fun `logout revokes the refresh token and clears local tokens`() = runTest {
        val storage = InMemoryTokenStorage(access = "a1", refresh = "r1")
        val recorded = RecordedRequests()
        val repo = AuthRepositoryImpl(
            mockApi(recorded, storage) { respondJson("""{"message":"Sesión cerrada exitosamente"}""") },
            storage,
        )

        repo.logout()

        assertEquals(logoutPath, recorded.last.url.encodedPath)
        assertEquals("""{"refresh_token":"r1"}""", recorded.last.jsonBody)
        assertNull(recorded.last.headers[HttpHeaders.Authorization])
        assertNull(storage.accessToken)
        assertNull(storage.refreshToken)
    }

    @Test
    fun `a rejected login does not try to refresh the stored session`() = runTest {
        val storage = InMemoryTokenStorage(access = "a1", refresh = "r1")
        val recorded = RecordedRequests()
        val repo = AuthRepositoryImpl(
            mockApi(recorded, storage) { request ->
                when (request.url.encodedPath) {
                    loginPath -> respondJson(INVALID_CREDENTIALS, HttpStatusCode.Unauthorized)
                    else -> error("Unexpected ${request.url}")
                }
            },
            storage,
        )

        val response = repo.login(LoginRequest("ana@correo.com", "equivocada"))

        assertFalse(response.success)
        assertEquals(listOf(loginPath), recorded.all.map { it.url.encodedPath })
        assertEquals("r1", storage.refreshToken)
    }

    @Test
    fun `logout clears local tokens even when the server is unreachable`() = runTest {
        val storage = InMemoryTokenStorage(access = "a1", refresh = "r1")
        val repo = AuthRepositoryImpl(mockApi(tokenStorage = storage) { throw IOException("offline") }, storage)

        repo.logout()

        assertNull(storage.accessToken)
        assertNull(storage.refreshToken)
    }

    private fun tokens(access: String, refresh: String) =
        """{"access_token":"$access","refresh_token":"$refresh"}"""

    private companion object {
        const val INVALID_CREDENTIALS =
            """{"statusCode":401,"message":"Invalid credentials","timestamp":"t","path":"/auth/login"}"""
    }
}
