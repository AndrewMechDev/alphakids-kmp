package org.alphakids.app.onboarding.domain.repository

import org.alphakids.app.onboarding.domain.model.AuthResponse
import org.alphakids.app.onboarding.domain.model.LoginRequest
import org.alphakids.app.onboarding.domain.model.RegisterRequest

/**
 * Repository interface for parent authentication.
 *
 * The backend issues tokens directly on login and register; there is no
 * email or OTP verification step.
 */
interface AuthRepository {

    /** Authenticate a parent with email and password. */
    suspend fun login(request: LoginRequest): AuthResponse

    /** Register a new parent account. */
    suspend fun register(request: RegisterRequest): AuthResponse

    /** Check if a user is currently logged in. */
    suspend fun isLoggedIn(): Boolean

    /** Log out the current user. */
    suspend fun logout()
}
