package org.alphakids.app.onboarding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.alphakids.app.onboarding.data.mock.MockAuthRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class VerificationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state with 6 empty digits`() {
        val viewModel = VerificationViewModel(MockAuthRepository())
        assertEquals("      ", viewModel.uiState.value.code)
        assertEquals(6, viewModel.uiState.value.code.length)
    }

    @Test
    fun `digit input updates correct position`() {
        val viewModel = VerificationViewModel(MockAuthRepository())
        viewModel.onCodeChanged("1")
        assertEquals("1     ", viewModel.uiState.value.code)

        viewModel.onCodeChanged("12")
        assertEquals("12    ", viewModel.uiState.value.code)

        viewModel.onCodeChanged("123")
        assertEquals("123   ", viewModel.uiState.value.code)

        viewModel.onCodeChanged("123456")
        assertEquals("123456", viewModel.uiState.value.code)
    }

    @Test
    fun `auto-advance to next digit works`() {
        val viewModel = VerificationViewModel(MockAuthRepository())
        viewModel.onCodeChanged("1")
        assertEquals(1, viewModel.uiState.value.code.trimEnd().length)

        viewModel.onCodeChanged("12")
        assertEquals(2, viewModel.uiState.value.code.trimEnd().length)

        viewModel.onCodeChanged("123")
        assertEquals(3, viewModel.uiState.value.code.trimEnd().length)

        viewModel.onCodeChanged("1234")
        assertEquals(4, viewModel.uiState.value.code.trimEnd().length)

        viewModel.onCodeChanged("12345")
        assertEquals(5, viewModel.uiState.value.code.trimEnd().length)

        viewModel.onCodeChanged("123456")
        assertEquals(6, viewModel.uiState.value.code.trimEnd().length)
    }

    @Test
    fun `verify with correct 6 digits succeeds`() {
        val repo = MockAuthRepository()
        val viewModel = VerificationViewModel(repo)
        viewModel.initialize("test@test.com")
        testDispatcher.scheduler.advanceUntilIdle()

        // Calling sendOtp again during cooldown returns the same code
        val otpCode = runBlocking { repo.sendOtp("test@test.com") }.code

        viewModel.onCodeChanged(otpCode)
        viewModel.onVerifyClick()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isVerified)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `verify with wrong digits shows error`() {
        val repo = MockAuthRepository()
        val viewModel = VerificationViewModel(repo)
        viewModel.initialize("test@test.com")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onCodeChanged("000000")
        viewModel.onVerifyClick()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Código incorrecto", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `resend code generates new code`() {
        val repo = MockAuthRepository()
        val viewModel = VerificationViewModel(repo)
        viewModel.initialize("test@test.com")
        testDispatcher.scheduler.advanceUntilIdle()

        // Advance tick counter past the 30-tick resend cooldown
        runBlocking { repeat(35) { repo.sendOtp("dummy$it@test.com") } }

        viewModel.onResendClick()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `resend cooldown timer counts down`() {
        val repo = MockAuthRepository()
        val viewModel = VerificationViewModel(repo)
        viewModel.initialize("test@test.com")
        testDispatcher.scheduler.advanceUntilIdle()

        // Advance tick counter past the 30-tick resend cooldown
        runBlocking { repeat(35) { repo.sendOtp("dummy$it@test.com") } }

        viewModel.onResendClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // After the 30s cooldown timer completes (virtual time with TestDispatcher)
        assertEquals(0, viewModel.uiState.value.resendCooldown)
        assertTrue(viewModel.uiState.value.canResend)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `verify with incomplete code shows validation error`() {
        val viewModel = VerificationViewModel(MockAuthRepository())
        viewModel.initialize("test@test.com")
        viewModel.onCodeChanged("123")

        // Only 3 digits entered, onVerifyClick should show error
        viewModel.onVerifyClick()
        assertEquals("Ingresa el código completo de 6 dígitos", viewModel.uiState.value.error)
    }
}
