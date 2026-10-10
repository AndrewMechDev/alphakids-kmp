package org.alphakids.app.domain.error

/**
 * Friendly Spanish text for each [DataError], shared by every screen.
 *
 * The backend's own messages are never shown: they are English or technical.
 * Auth keeps its own credential-specific wording in `AuthErrorMessages`, which
 * reuses the connection/server texts defined here.
 */
object DataErrorMessages {
    const val NO_CONNECTION = "No pudimos conectarnos. Revisa tu conexión a internet e inténtalo de nuevo."
    const val TIMEOUT = "El servidor tardó demasiado en responder. Inténtalo de nuevo."
    const val SESSION_EXPIRED = "Tu sesión expiró. Vuelve a iniciar sesión."
    const val NOT_FOUND = "No encontramos esta información. Puede que ya no exista."
    const val SERVER_UNAVAILABLE = "El servicio no está disponible en este momento. Inténtalo más tarde."
    const val INVALID_DATA = "No pudimos guardar los datos. Revísalos e inténtalo de nuevo."
    const val UNSUPPORTED = "Esta función aún no está disponible."
    const val UNEXPECTED = "Ocurrió un error inesperado. Inténtalo de nuevo."
}

/** The message to show for this error. */
val DataError.userMessage: String
    get() = when (this) {
        DataError.Network -> DataErrorMessages.NO_CONNECTION
        DataError.Timeout -> DataErrorMessages.TIMEOUT
        DataError.Unauthorized -> DataErrorMessages.SESSION_EXPIRED
        DataError.NotFound -> DataErrorMessages.NOT_FOUND
        is DataError.Server -> DataErrorMessages.SERVER_UNAVAILABLE
        is DataError.Validation -> DataErrorMessages.INVALID_DATA
        DataError.Unsupported -> DataErrorMessages.UNSUPPORTED
        DataError.Unknown -> DataErrorMessages.UNEXPECTED
    }

/** The message to show for a failed result; [DataErrorMessages.UNEXPECTED] if it did not fail. */
val Result<*>.errorMessage: String
    get() = dataError?.userMessage ?: DataErrorMessages.UNEXPECTED

/** The message of the first failed result, or null when every result succeeded. */
fun firstErrorMessage(results: List<Result<*>>): String? =
    results.firstOrNull { it.isFailure }?.errorMessage

/** The message to show for a failed result, or null when it succeeded. */
val Result<*>.errorMessageOrNull: String?
    get() = if (isFailure) errorMessage else null
