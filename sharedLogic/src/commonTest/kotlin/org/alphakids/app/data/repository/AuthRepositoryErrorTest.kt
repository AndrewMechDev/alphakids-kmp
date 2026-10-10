package org.alphakids.app.data.repository

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import org.alphakids.app.data.remote.InMemoryTokenStorage
import org.alphakids.app.data.remote.mockApi
import org.alphakids.app.data.remote.respondJson
import org.alphakids.app.onboarding.domain.model.AuthResponse
import org.alphakids.app.onboarding.domain.model.LoginRequest
import org.alphakids.app.onboarding.domain.model.RegisterRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * Auth failures reach the parent as friendly Spanish messages, never as raw
 * exception text or the backend's English/technical messages.
 */
class AuthRepositoryErrorTest {

    @Test
    fun `wrong credentials`() = runTest {
        val response = login { respondJson(apiError(401, "Invalid credentials"), HttpStatusCode.Unauthorized) }

        assertFailure("Correo o contraseña incorrectos.", response)
    }

    @Test
    fun `a login the backend rejects as invalid input reads as wrong credentials`() = runTest {
        // e.g. a 7-character password fails the backend's min(8) before any lookup.
        val response = login { respondJson(apiError(400, "Validation failed"), HttpStatusCode.BadRequest) }

        assertFailure("Correo o contraseña incorrectos.", response)
    }

    @Test
    fun `email already registered`() = runTest {
        val response = register { respondJson(apiError(400, "El correo ya está registrado"), HttpStatusCode.BadRequest) }

        assertFailure("Este correo ya está registrado. Inicia sesión o usa otro correo.", response)
    }

    @Test
    fun `registration data rejected by validation`() = runTest {
        val response = register { respondJson(apiError(400, "Validation failed"), HttpStatusCode.BadRequest) }

        assertFailure(
            "Revisa tus datos: el correo debe ser válido y la contraseña tener al menos 8 caracteres.",
            response,
        )
    }

    @Test
    fun `too many attempts`() = runTest {
        val response = login {
            respondJson(apiError(429, "ThrottlerException: Too Many Requests"), HttpStatusCode.TooManyRequests)
        }

        assertFailure("Demasiados intentos. Espera un momento y vuelve a intentarlo.", response)
    }

    @Test
    fun `server error`() = runTest {
        val response = register {
            respondJson(apiError(500, "Error interno del servidor"), HttpStatusCode.InternalServerError)
        }

        assertFailure("El servicio no está disponible en este momento. Inténtalo más tarde.", response)
    }

    @Test
    fun `error body that is not json`() = runTest {
        val response = login { respondJson("<html>Bad gateway</html>", HttpStatusCode.BadGateway) }

        assertFailure("El servicio no está disponible en este momento. Inténtalo más tarde.", response)
    }

    @Test
    fun `no connection`() = runTest {
        val response = login { throw IOException("Unable to resolve host alphakids-api.onrender.com") }

        assertFailure("No pudimos conectarnos. Revisa tu conexión a internet e inténtalo de nuevo.", response)
    }

    @Test
    fun `timeout`() = runTest {
        val response = login { throw HttpRequestTimeoutException("https://api.test/auth/login", 30_000) }

        assertFailure("El servidor tardó demasiado en responder. Inténtalo de nuevo.", response)
    }

    @Test
    fun `malformed success body does not store tokens`() = runTest {
        val storage = InMemoryTokenStorage()
        val repo = AuthRepositoryImpl(mockApi(tokenStorage = storage) { respondJson("""{"unexpected":true}""") }, storage)

        val response = repo.login(LoginRequest("ana@correo.com", "secreto123"))

        assertFailure("Ocurrió un error inesperado. Inténtalo de nuevo.", response)
        assertNull(storage.accessToken)
    }

    private suspend fun login(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): AuthResponse {
        val storage = InMemoryTokenStorage()
        return AuthRepositoryImpl(mockApi(tokenStorage = storage, handler = handler), storage)
            .login(LoginRequest("ana@correo.com", "secreto123"))
    }

    private suspend fun register(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): AuthResponse {
        val storage = InMemoryTokenStorage()
        return AuthRepositoryImpl(mockApi(tokenStorage = storage, handler = handler), storage)
            .register(RegisterRequest("Ana", "ana@correo.com", "999999999", "secreto123"))
    }

    private fun assertFailure(expectedMessage: String, response: AuthResponse) {
        assertFalse(response.success)
        assertEquals(expectedMessage, response.message)
    }

    /** The backend's error shape (AllExceptionsFilter). */
    private fun apiError(status: Int, message: String) =
        """{"statusCode":$status,"message":"$message","timestamp":"2026-10-10T00:00:00.000Z","path":"/auth"}"""
}
