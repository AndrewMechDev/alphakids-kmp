package org.alphakids.app.data.repository

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException

/** Which credential call failed — the same status means different things. */
internal enum class AuthOperation { LOGIN, REGISTER }

/**
 * Friendly Spanish messages for failed login/register calls.
 *
 * The backend answers errors as `{statusCode, message}`, but its messages are
 * not meant for parents: login failures say "Invalid credentials", schema
 * failures say "Validation failed", and exception text is technical. Only the
 * meaning is taken from the response; the wording shown is always ours.
 */
internal object AuthErrorMessages {
    const val INVALID_CREDENTIALS = "Correo o contraseña incorrectos."
    const val EMAIL_TAKEN = "Este correo ya está registrado. Inicia sesión o usa otro correo."
    const val INVALID_REGISTRATION =
        "Revisa tus datos: el correo debe ser válido y la contraseña tener al menos 8 caracteres."
    const val TOO_MANY_ATTEMPTS = "Demasiados intentos. Espera un momento y vuelve a intentarlo."
    const val SERVER_UNAVAILABLE = "El servicio no está disponible en este momento. Inténtalo más tarde."
    const val TIMEOUT = "El servidor tardó demasiado en responder. Inténtalo de nuevo."
    const val NO_CONNECTION = "No pudimos conectarnos. Revisa tu conexión a internet e inténtalo de nuevo."
    const val UNEXPECTED = "Ocurrió un error inesperado. Inténtalo de nuevo."

    /** Backend text for a duplicate email (AuthService.register). */
    private const val BACKEND_EMAIL_TAKEN = "ya está registrado"

    /** Message for an HTTP error [status] whose body carried [backendMessage]. */
    fun forStatus(operation: AuthOperation, status: Int, backendMessage: String?): String = when {
        status == 429 -> TOO_MANY_ATTEMPTS
        status >= 500 -> SERVER_UNAVAILABLE
        operation == AuthOperation.LOGIN && (status == 400 || status == 401) -> INVALID_CREDENTIALS
        operation == AuthOperation.REGISTER && isEmailTaken(status, backendMessage) -> EMAIL_TAKEN
        operation == AuthOperation.REGISTER && status == 400 -> INVALID_REGISTRATION
        else -> UNEXPECTED
    }

    /** Message for a call that never produced an HTTP response. */
    fun forFailure(cause: Throwable): String = when (cause) {
        is HttpRequestTimeoutException, is ConnectTimeoutException, is SocketTimeoutException -> TIMEOUT
        else -> NO_CONNECTION
    }

    private fun isEmailTaken(status: Int, backendMessage: String?): Boolean =
        status == 409 || (status == 400 && backendMessage?.contains(BACKEND_EMAIL_TAKEN) == true)
}
