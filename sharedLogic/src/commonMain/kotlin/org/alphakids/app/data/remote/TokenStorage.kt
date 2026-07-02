package org.alphakids.app.data.remote

/**
 * Thread-safe token storage for JWT access and refresh tokens.
 *
 * Default implementation is in-memory. Swap to [multiplatform-settings]
 * for persistent storage across app restarts.
 */
interface TokenStorage {
    var accessToken: String?
    var refreshToken: String?
    val isLoggedIn: Boolean

    fun clear()
}

/**
 * In-memory [TokenStorage] implementation.
 *
 * Tokens survive only for the current app session.
 * Replace with multiplatform-settings-backed storage for persistence.
 */
class InMemoryTokenStorage : TokenStorage {
    override var accessToken: String? = null
    override var refreshToken: String? = null
    override val isLoggedIn: Boolean get() = accessToken != null

    override fun clear() {
        accessToken = null
        refreshToken = null
    }
}
