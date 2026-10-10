package org.alphakids.app.onboarding.domain.usecase

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.alphakids.app.onboarding.domain.repository.AuthRepository
import org.alphakids.app.parent.domain.model.SessionManager

/**
 * The single sign-out path for every "Cerrar sesión" entry point.
 *
 * Forgets the in-memory session (active child plus everything cached for
 * that child, see [SessionManager.clearSession]) and signs out of the
 * backend. It runs as [NonCancellable] work: callers typically navigate away
 * right after, and a cancelled sign-out would leave the refresh token alive
 * on the server.
 */
class LogoutUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke() {
        withContext(NonCancellable) {
            SessionManager.clearSession()
            authRepository.logout()
        }
    }
}
