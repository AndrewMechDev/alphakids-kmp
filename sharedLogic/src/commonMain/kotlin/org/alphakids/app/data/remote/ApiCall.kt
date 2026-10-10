package org.alphakids.app.data.remote

import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.io.IOException
import org.alphakids.app.data.remote.dto.ApiErrorDto
import org.alphakids.app.domain.error.DataError
import org.alphakids.app.domain.error.DataException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs one backend call and returns its outcome as a [Result] whose failure
 * is always a [DataException].
 *
 * [CancellationException] is rethrown, never turned into a failure: it means
 * the caller's coroutine was cancelled (e.g. the screen left), and catching it
 * would let that coroutine keep running as if the request had just failed.
 * Errors (`Throwable` that is not an `Exception`) are not caught either.
 */
internal inline fun <T> apiCall(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e.toDataException())
}

/**
 * The decoded body of a 2xx response; otherwise throws a [DataException]
 * mapped from the status (see [httpError]).
 */
internal suspend inline fun <reified T> HttpResponse.bodyOrThrow(): T {
    throwIfFailed()
    return body()
}

/** Throws a [DataException] for a non-2xx response. */
internal suspend fun HttpResponse.throwIfFailed() {
    if (!status.isSuccess()) throw DataException(httpError(status.value, backendMessage()))
}

/** [DataError] for an HTTP error [status] whose body carried [backendMessage]. */
internal fun httpError(status: Int, backendMessage: String?): DataError = when (status) {
    401, 403 -> DataError.Unauthorized
    404 -> DataError.NotFound
    408 -> DataError.Timeout
    400, 409, 422 -> DataError.Validation(backendMessage)
    in 500..599 -> DataError.Server(status)
    else -> DataError.Unknown
}

/** Wraps an exception thrown by a backend call, keeping it as the cause. */
internal fun Exception.toDataException(): DataException =
    this as? DataException ?: DataException(toDataError(), cause = this)

/** [DataError] for a call that never produced a usable HTTP response. */
internal fun Throwable.toDataError(): DataError = when {
    isTimeout() -> DataError.Timeout
    this is IOException -> DataError.Network
    else -> DataError.Unknown // e.g. a body that does not match the DTO
}

/** Whether this exception means the server did not answer in time. */
internal fun Throwable.isTimeout(): Boolean =
    this is HttpRequestTimeoutException || this is ConnectTimeoutException || this is SocketTimeoutException

/**
 * The `message` of the backend's `{statusCode, message, timestamp, path}`
 * error body (AllExceptionsFilter), or null if the body is not that shape.
 */
internal suspend fun HttpResponse.backendMessage(): String? = try {
    body<ApiErrorDto>().message
} catch (e: CancellationException) {
    throw e
} catch (_: Exception) {
    null
}
