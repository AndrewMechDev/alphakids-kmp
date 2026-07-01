package org.alphakids.app.onboarding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class LoginViewModelTest {

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
    fun `initial state is correct`() {
        val viewModel = LoginViewModel(MockAuthRepository())
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.isSuccess)
    }

    @Test
    fun `onEmailChanged updates email`() {
        val viewModel = LoginViewModel(MockAuthRepository())
        viewModel.onEmailChanged("test@alphakids.com")
        assertEquals("test@alphakids.com", viewModel.uiState.value.email)
    }

    @Test
    fun `onPasswordChanged updates password`() {
        val viewModel = LoginViewModel(MockAuthRepository())
        viewModel.onPasswordChanged("123456")
        assertEquals("123456", viewModel.uiState.value.password)
    }

    @Test
    fun `login with valid credentials succeeds`() {
        val repo = MockAuthRepository.createWithDemoUser()
        val viewModel = LoginViewModel(repo)

        viewModel.onEmailChanged("test@alphakids.com")
        viewModel.onPasswordChanged("123456")
        viewModel.onLoginClick()

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `login with invalid email shows validation error`() {
        val viewModel = LoginViewModel(MockAuthRepository())
        viewModel.onEmailChanged("invalid")
        viewModel.onPasswordChanged("123456")
        viewModel.onLoginClick()

        assertEquals("Formato de email inválido", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `login with empty password shows validation error`() {
        val viewModel = LoginViewModel(MockAuthRepository())
        viewModel.onEmailChanged("test@test.com")
        viewModel.onPasswordChanged("")
        viewModel.onLoginClick()

        assertEquals("La contraseña es requerida", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `login with wrong credentials shows error message`() {
        val repo = MockAuthRepository.createWithDemoUser()
        val viewModel = LoginViewModel(repo)

        viewModel.onEmailChanged("test@alphakids.com")
        viewModel.onPasswordChanged("wrongpass")
        viewModel.onLoginClick()

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Contraseña incorrecta", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loading state is true during login`() {
        val repo = MockAuthRepository.createWithDemoUser()
        val viewModel = LoginViewModel(repo)

        viewModel.onEmailChanged("test@alphakids.com")
        viewModel.onPasswordChanged("123456")
        viewModel.onLoginClick()

        // isLoading should be true immediately before the coroutine completes
        assertTrue(viewModel.uiState.value.isLoading)

        testDispatcher.scheduler.advanceUntilIdle()

        // After completion, loading should be false
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
