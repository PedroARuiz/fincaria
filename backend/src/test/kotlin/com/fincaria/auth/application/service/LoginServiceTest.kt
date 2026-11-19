package com.fincaria.auth.application.service

import com.fincaria.auth.application.port.`in`.LoginCommand
import com.fincaria.auth.application.port.out.IssuedToken
import com.fincaria.auth.application.port.out.TokenIssuer
import com.fincaria.auth.domain.exception.InvalidCredentialsException
import com.fincaria.users.application.port.out.PasswordHasher
import com.fincaria.users.application.port.out.UserRepository
import com.fincaria.users.domain.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LoginServiceTest {

    val userRepository: UserRepository = mockk()
    val passwordHasher: PasswordHasher = mockk()
    val tokenIssuer: TokenIssuer = mockk()
    val service = LoginService(
        userRepository = userRepository,
        passwordHasher = passwordHasher,
        tokenIssuer = tokenIssuer,
    )

    val existingUser = User(id = "user-id", email = "pedro@example.com", name = "Pedro", passwordHash = "hashed")

    @BeforeEach
    fun setup() {
        every { userRepository.findByEmail(existingUser.email) } answers { existingUser }
    }

    @Test
    fun `logging in with correct credentials returns the issued token`() {
        every { passwordHasher.matches("raw-password", existingUser.passwordHash) } answers { true }
        every { tokenIssuer.issue(existingUser.id) } answers { IssuedToken(value = "jwt-token", expiresInSeconds = 3600) }

        val token = service(LoginCommand(email = existingUser.email, password = "raw-password"))

        assertEquals("jwt-token", token.value)
        assertEquals(3600, token.expiresInSeconds)
    }

    @Test
    fun `logging in with an unknown email throws InvalidCredentialsException`() {
        every { userRepository.findByEmail("unknown@example.com") } answers { null }

        assertThrows<InvalidCredentialsException> {
            service(LoginCommand(email = "unknown@example.com", password = "raw-password"))
        }
        verify(exactly = 0) { tokenIssuer.issue(any()) }
    }

    @Test
    fun `logging in with the wrong password throws InvalidCredentialsException`() {
        every { passwordHasher.matches("wrong-password", existingUser.passwordHash) } answers { false }

        assertThrows<InvalidCredentialsException> {
            service(LoginCommand(email = existingUser.email, password = "wrong-password"))
        }
        verify(exactly = 0) { tokenIssuer.issue(any()) }
    }
}
