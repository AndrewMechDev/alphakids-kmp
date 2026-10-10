package org.alphakids.app.onboarding.domain.usecase

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.alphakids.app.analytics.AchievementAnalytics
import org.alphakids.app.game.domain.model.GameSessionState
import org.alphakids.app.onboarding.domain.model.LoginRequest
import org.alphakids.app.onboarding.domain.model.RegisterRequest
import org.alphakids.app.onboarding.domain.repository.AuthRepository
import org.alphakids.app.parent.domain.model.ChildSummary
import org.alphakids.app.parent.domain.model.GameProgressManager
import org.alphakids.app.parent.domain.model.SessionManager
import org.alphakids.app.parent.domain.model.child
import org.alphakids.app.parent.domain.model.progress
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LogoutUseCaseTest {

    @AfterTest
    fun tearDown() {
        SessionManager.clearSession()
        GameProgressManager.clear()
        AchievementAnalytics.clear()
        GameSessionState.clear()
    }

    @Test
    fun `logout signs out and forgets every piece of session state`() = runTest {
        val repo = RecordingAuthRepository()
        startSessionFor(child("child-1"))

        LogoutUseCase(repo)()

        assertTrue(repo.loggedOut)
        assertSessionIsEmpty()
    }

    @Test
    fun `logout completes even when the caller is cancelled mid-way`() = runTest {
        // Regression: the parent dashboard launched logout in the screen's
        // scope and navigated away at once, cancelling the revocation.
        val revocation = CompletableDeferred<Unit>()
        val repo = RecordingAuthRepository(onLogout = { revocation.await() })
        startSessionFor(child("child-1"))

        val job = launch { LogoutUseCase(repo)() }
        runCurrent()
        job.cancel()
        revocation.complete(Unit)
        job.join()

        assertTrue(repo.loggedOut)
        assertSessionIsEmpty()
    }

    private fun startSessionFor(child: ChildSummary) {
        SessionManager.setActiveChild(child)
        GameProgressManager.applyServerProgress(progress(child.id))
        AchievementAnalytics.trackStarsEarned(amount = 3, source = "escaneo")
        GameSessionState.setWord(text = "GATO", id = "w1")
    }

    private fun assertSessionIsEmpty() {
        assertFalse(SessionManager.hasActiveChild)
        assertNull(GameProgressManager.economy.value)
        assertEquals(0, AchievementAnalytics.totalEvents())
        assertEquals("", GameSessionState.currentWordText)
    }

    private class RecordingAuthRepository(
        private val onLogout: suspend () -> Unit = {},
    ) : AuthRepository {
        var loggedOut = false

        override suspend fun logout() {
            onLogout()
            loggedOut = true
        }

        override suspend fun login(request: LoginRequest) = error("unused")
        override suspend fun register(request: RegisterRequest) = error("unused")
        override suspend fun isLoggedIn() = !loggedOut
        override suspend fun sendOtp(email: String) = error("unused")
        override suspend fun verifyOtp(email: String, code: String) = error("unused")
        override suspend fun resendOtp(email: String) = error("unused")
    }
}
