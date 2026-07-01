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
class RegisterViewModelTest {

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
        val viewModel = RegisterViewModel(MockAuthRepository())
        val state = viewModel.uiState.value
        assertEquals("", state.name)
        assertEquals("", state.email)
        assertEquals("", state.phone)
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)
        assertFalse(state.termsAccepted)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.isSuccess)
    }

    @Test
    fun `all field updates work`() {
        val viewModel = RegisterViewModel(MockAuthRepository())

        viewModel.onNameChanged("Test User")
        viewModel.onEmailChanged("test@test.com")
        viewModel.onPhoneChanged("123456789")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")
        viewModel.onTermsChanged(true)

        val state = viewModel.uiState.value
        assertEquals("Test User", state.name)
        assertEquals("test@test.com", state.email)
        assertEquals("123456789", state.phone)
        assertEquals("password123", state.password)
        assertEquals("password123", state.confirmPassword)
        assertTrue(state.termsAccepted)
    }

    @Test
    fun `register with valid data succeeds`() {
        val repo = MockAuthRepository()
        val viewModel = RegisterViewModel(repo)

        viewModel.onNameChanged("Test Parent")
        viewModel.onEmailChanged("parent@test.com")
        viewModel.onPhoneChanged("123456789")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")
        viewModel.onTermsChanged(true)
        viewModel.onRegisterClick()

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `register with mismatched passwords shows error`() {
        val viewModel = RegisterViewModel(MockAuthRepository())

        viewModel.onNameChanged("Test")
        viewModel.onEmailChanged("test@test.com")
        viewModel.onPhoneChanged("123456789")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("different")
        viewModel.onTermsChanged(true)
        viewModel.onRegisterClick()

        assertEquals("Las contraseñas no coinciden", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `register with empty name shows error`() {
        val viewModel = RegisterViewModel(MockAuthRepository())

        viewModel.onEmailChanged("test@test.com")
        viewModel.onPhoneChanged("123456789")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")
        viewModel.onTermsChanged(true)
        viewModel.onRegisterClick()

        assertEquals("El nombre es requerido", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `register with invalid email shows error`() {
        val viewModel = RegisterViewModel(MockAuthRepository())

        viewModel.onNameChanged("Test")
        viewModel.onEmailChanged("invalid")
        viewModel.onPhoneChanged("123456789")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")
        viewModel.onTermsChanged(true)
        viewModel.onRegisterClick()

        assertEquals("Formato de email inválido", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `register without accepting terms shows error`() {
        val viewModel = RegisterViewModel(MockAuthRepository())

        viewModel.onNameChanged("Test")
        viewModel.onEmailChanged("test@test.com")
        viewModel.onPhoneChanged("123456789")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")
        viewModel.onRegisterClick()

        assertEquals("Debes aceptar los términos y condiciones", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `terms acceptance toggle works`() {
        val viewModel = RegisterViewModel(MockAuthRepository())

        assertFalse(viewModel.uiState.value.termsAccepted)

        viewModel.onTermsChanged(true)
        assertTrue(viewModel.uiState.value.termsAccepted)

        viewModel.onTermsChanged(false)
        assertFalse(viewModel.uiState.value.termsAccepted)
    }
}
