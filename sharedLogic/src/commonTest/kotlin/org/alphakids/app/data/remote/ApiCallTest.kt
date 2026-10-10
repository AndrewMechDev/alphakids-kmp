package org.alphakids.app.data.remote

import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import org.alphakids.app.domain.error.DataError
import org.alphakids.app.domain.error.DataErrorMessages
import org.alphakids.app.domain.error.dataError
import org.alphakids.app.domain.error.dataFailure
import org.alphakids.app.domain.error.errorMessage
import org.alphakids.app.domain.error.userMessage
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/** Mapping of backend outcomes to [DataError] (the shape every repository returns). */
class ApiCallTest {

    @Serializable
    private data class Thing(val id: String)

    @Test
    fun a_2xx_response_is_decoded() = runTest {
        val result = call { respondJson("""{"id":"t_1"}""") }

        assertEquals(Thing("t_1"), result.getOrThrow())
        assertNull(result.dataError)
    }

    @Test
    fun http_statuses_map_to_typed_errors() = runTest {
        suspend fun errorFor(status: HttpStatusCode, body: String = "{}") =
            call { respondJson(body, status) }.dataError

        assertEquals(DataError.NotFound, errorFor(HttpStatusCode.NotFound))
        assertEquals(DataError.Server(500), errorFor(HttpStatusCode.InternalServerError))
        assertEquals(DataError.Server(503), errorFor(HttpStatusCode.ServiceUnavailable))
        assertEquals(DataError.Unauthorized, errorFor(HttpStatusCode.Forbidden))
        assertEquals(DataError.Timeout, errorFor(HttpStatusCode.RequestTimeout))
        assertEquals(DataError.Unknown, errorFor(HttpStatusCode.TooManyRequests))
        assertEquals(
            DataError.Validation("Validation failed"),
            errorFor(HttpStatusCode.BadRequest, BackendFixtures.badRequest("Validation failed")),
        )
        assertEquals(DataError.Validation(null), errorFor(HttpStatusCode.Conflict, "<html>conflict</html>"))
    }

    @Test
    fun a_401_that_the_token_refresh_cannot_fix_is_unauthorized() = runTest {
        // Every request, including POST /auth/refresh, is rejected.
        val result = call { respondJson("{}", HttpStatusCode.Unauthorized) }

        assertEquals(DataError.Unauthorized, result.dataError)
    }

    @Test
    fun no_connection_is_a_network_error() = runTest {
        val result = call { throw IOException("Unable to resolve host") }

        assertEquals(DataError.Network, result.dataError)
    }

    @Test
    fun timeouts_are_timeout_errors() = runTest {
        assertEquals(DataError.Timeout, call { throw HttpRequestTimeoutException("https://api.test/x", 30_000) }.dataError)
        assertEquals(DataError.Timeout, call { throw ConnectTimeoutException("connect timeout") }.dataError)
    }

    @Test
    fun a_body_that_does_not_match_the_dto_is_unknown() = runTest {
        val result = call { respondJson("""{"unexpected":true}""") }

        assertEquals(DataError.Unknown, result.dataError)
    }

    @Test
    fun cancellation_is_rethrown_not_returned_as_a_failure() {
        assertFailsWith<CancellationException> {
            apiCall<Unit> { throw CancellationException("screen left") }
        }
    }

    @Test
    fun every_error_has_a_friendly_spanish_message() {
        assertEquals(DataErrorMessages.NO_CONNECTION, DataError.Network.userMessage)
        assertEquals(DataErrorMessages.SERVER_UNAVAILABLE, DataError.Server(502).userMessage)
        // The backend's English text never reaches the screen.
        assertEquals(DataErrorMessages.INVALID_DATA, DataError.Validation("Validation failed").userMessage)
        assertEquals(DataErrorMessages.UNSUPPORTED, dataFailure(DataError.Unsupported).errorMessage)
    }

    private suspend fun call(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): Result<Thing> {
        val api = mockApi(handler = handler)
        return apiCall { api.httpClient.get("/things/t_1").bodyOrThrow<Thing>() }
    }
}
