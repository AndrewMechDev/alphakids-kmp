package org.alphakids.app.onboarding.data.mock

import kotlinx.coroutines.runBlocking
import org.alphakids.app.onboarding.domain.model.LoginRequest
import org.alphakids.app.onboarding.domain.model.OTPCode
import org.alphakids.app.onboarding.domain.model.RegisterRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
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
    fun `sendOtp generates 6-digit code`() = runBlocking {
        val repo = MockAuthRepository()
        val result = repo.sendOtp("test@test.com")
        assertEquals(6, result.code.length)
        assertTrue(result.code.all { it.isDigit() })
    }

    @Test
    fun `verifyOtp with correct code returns true`() = runBlocking {
        val repo = MockAuthRepository()
        val otpResult = repo.sendOtp("test@test.com")
        assertTrue(repo.verifyOtp("test@test.com", otpResult.code))
    }

    @Test
    fun `verifyOtp with wrong code returns false`() = runBlocking {
        val repo = MockAuthRepository()
        repo.sendOtp("test@test.com")
        assertFalse(repo.verifyOtp("test@test.com", "000000"))
    }

    @Test
    fun `resendOtp generates new code`() = runBlocking {
        val repo = MockAuthRepository()
        val first = repo.sendOtp("test@test.com")

        // Advance tick counter past the 30-tick cooldown
        repeat(35) { repo.sendOtp("dummy$it@test.com") }

        val second = repo.resendOtp("test@test.com")
        assertNotEquals(first.code, second.code)
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

    @Test
    fun `OTP code is exactly 6 digits`() = runBlocking {
        val repo = MockAuthRepository()
        val result = repo.sendOtp("test@test.com")
        assertEquals(6, result.code.length)
        assertTrue(result.code.all { it.isDigit() })

        // Verify the OTP code value object also accepts it
        val otpCode = OTPCode(result.code)
        assertEquals(result.code, otpCode.value)
    }

    @Test
    fun `3 failed OTP attempts require resend`() = runBlocking {
        val repo = MockAuthRepository()
        repo.sendOtp("test@test.com")

        // First 3 attempts fail (incorrect code)
        assertFalse(repo.verifyOtp("test@test.com", "000000"))
        assertFalse(repo.verifyOtp("test@test.com", "000000"))
        assertFalse(repo.verifyOtp("test@test.com", "000000"))

        // 4th attempt also fails because attempts are exhausted (max 3)
        assertFalse(repo.verifyOtp("test@test.com", "000000"))

        // Advance tick counter past cooldown
        repeat(35) { repo.sendOtp("dummy$it@test.com") }

        // Resend (now past cooldown) resets attempts and generates new code
        val otpResult = repo.resendOtp("test@test.com")

        // Verify with correct code should now work
        assertTrue(repo.verifyOtp("test@test.com", otpResult.code))
    }
}
