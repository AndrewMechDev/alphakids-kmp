package org.alphakids.app.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.content.TextContent
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

/** Records every request the client sent so tests can assert the wire contract. */
class RecordedRequests {
    val all = mutableListOf<HttpRequestData>()
    val last: HttpRequestData get() = all.last()
}

/** Body of a JSON request as sent on the wire. */
val HttpRequestData.jsonBody: String
    get() = (body as TextContent).text

/**
 * Builds an [AlphaKidsApiClient] backed by Ktor's [MockEngine]. The real
 * client configuration (JSON, auth, default request) is kept, so these tests
 * exercise the same serialization the app ships with.
 */
fun mockApi(
    recorded: RecordedRequests = RecordedRequests(),
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
): AlphaKidsApiClient {
    val engine = MockEngine { request ->
        recorded.all += request
        handler(request)
    }
    return AlphaKidsApiClient(FakeTokenStorage(), baseUrl = "https://api.test", engine = engine)
}

fun MockRequestHandleScope.respondJson(
    json: String,
    status: HttpStatusCode = HttpStatusCode.OK,
): HttpResponseData = respond(
    content = json,
    status = status,
    headers = headersOf(HttpHeaders.ContentType, "application/json"),
)

private class FakeTokenStorage : TokenStorage {
    override var accessToken: String? = "access"
    override var refreshToken: String? = "refresh"
    override val isLoggedIn: Boolean get() = accessToken != null
    override fun clear() {
        accessToken = null
        refreshToken = null
    }
}
