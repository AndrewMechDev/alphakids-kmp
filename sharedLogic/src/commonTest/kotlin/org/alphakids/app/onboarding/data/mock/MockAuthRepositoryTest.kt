package org.alphakids.app.onboarding.data.mock

import kotlinx.coroutines.runBlocking
import org.alphakids.app.onboarding.domain.model.LoginRequest
import org.alphakids.app.onboarding.domain.model.RegisterRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MockAuthRepositoryTest {

    @Test
    fun `register stores user correctly`() = runBlocking {
        val repo = MockAuthRepository()
        val response = repo.register(
            RegisterRequest(
                name = "Test Parent",
                email = "parent@test.com",
                phone = "123456789",
                password = "password123",
            ),
        )
        assertTrue(response.success)
        assertEquals("Registro exitoso", response.message)
        assertEquals("parent@test.com", response.email)
    }

    @Test
    fun `login with registered credentials succeeds`() = runBlocking {
        val repo = MockAuthRepository()
        repo.register(
            RegisterRequest(
                name = "Test User",
                email = "user@test.com",
                phone = "123456789",
                password = "pass123",
            ),
        )
        val response = repo.login(LoginRequest(email = "user@test.com", password = "pass123"))
        assertTrue(response.success)
        assertEquals("Inicio de sesión exitoso", response.message)
    }

    @Test
    fun `login with wrong password fails`() = runBlocking {
        val repo = MockAuthRepository()
        repo.register(
            RegisterRequest(
                name = "Test User",
                email = "user@test.com",
                phone = "123456789",
                password = "correctpass",
            ),
        )
        val response = repo.login(LoginRequest(email = "user@test.com", password = "wrongpass"))
        assertFalse(response.success)
        assertEquals("Contraseña incorrecta", response.message)
    }

    @Test
    fun `login with demo credentials works`() = runBlocking {
        val repo = MockAuthRepository.createWithDemoUser()
        val response = repo.login(
            LoginRequest(email = "test@alphakids.com", password = "123456"),
        )
        assertTrue(response.success)
        assertEquals("Inicio de sesión exitoso", response.message)
    }

    @Test
    fun `validation catches invalid email format`() = runBlocking {
        val repo = MockAuthRepository()
        val response = repo.register(
            RegisterRequest(
                name = "Test",
                email = "invalid-email",
                phone = "123456789",
                password = "123456",
            ),
        )
        assertFalse(response.success)
        assertEquals("Formato de email inválido", response.message)
    }
}
