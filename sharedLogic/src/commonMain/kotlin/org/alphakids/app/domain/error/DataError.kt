package org.alphakids.app.domain.error

/**
 * Why a data operation failed, in terms a screen can act on.
 *
 * Repositories return `Result<T>`; a failed result always carries a
 * [DataException] holding one of these. That lets the UI tell "the child has
 * no pets yet" (success, empty list) apart from "the server is down"
 * (failure), and pick a message and a retry for each case.
 */
sealed interface DataError {
    /** No connection: offline, DNS failure, connection refused or reset. */
    data object Network : DataError

    /** The server did not answer in time. */
    data object Timeout : DataError

    /** The session is no longer accepted (401/403 after the token refresh failed). */
    data object Unauthorized : DataError

    /** The requested resource does not exist (404). */
    data object NotFound : DataError

    /** The server failed while handling the request (5xx). */
    data class Server(val code: Int) : DataError

    /**
     * The server rejected the request data (400, 409, 422).
     *
     * [message] is the backend's own text, kept for diagnostics only: it is
     * English or technical (e.g. "Validation failed") and is never shown.
     */
    data class Validation(val message: String?) : DataError

    /** The backend has no endpoint for this operation yet. */
    data object Unsupported : DataError

    /** Anything else: an unexpected status or a response that could not be decoded. */
    data object Unknown : DataError
}

/** The failure inside a repository [Result]. The original exception, if any, is the [cause]. */
class DataException(
    val error: DataError,
    cause: Throwable? = null,
) : Exception(error.toString(), cause)

/** A failed [Result] carrying [error]. */
fun dataFailure(error: DataError): Result<Nothing> = Result.failure(DataException(error))

/**
 * The [DataError] of a failed result, or null on success. A failure that is
 * not a [DataException] (not produced by a repository) reads as [DataError.Unknown].
 */
val Result<*>.dataError: DataError?
    get() = exceptionOrNull()?.let { (it as? DataException)?.error ?: DataError.Unknown }
