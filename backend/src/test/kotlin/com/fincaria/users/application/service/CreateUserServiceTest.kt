package com.fincaria.users.application.service

import com.fincaria.users.application.port.`in`.CreateUserCommand
import com.fincaria.users.application.port.out.PasswordHasher
import com.fincaria.users.application.port.out.UserRepository
import com.fincaria.users.domain.exception.EmailAlreadyInUseException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CreateUserServiceTest {

    val userRepository: UserRepository = mockk()
    val passwordHasher: PasswordHasher = mockk()
    val service = CreateUserService(userRepository = userRepository, passwordHasher = passwordHasher)

    @BeforeEach
    fun setup() {
        every { userRepository.save(any()) } answers { firstArg() }
        every { userRepository.existsByEmail(any()) } answers { false }
        every { passwordHasher.hash(any()) } answers { "hashed-${firstArg<String>()}" }
    }

    @Test
    fun `creating a user with a new email saves and returns the user with the password hashed`() {
        val command = CreateUserCommand(
            name = "new-name",
            email = "new-email",
            password = "raw-password",
        )

        val user = service(command = command)

        assertEquals(command.name, user.name)
        assertEquals(command.email, user.email)
        assertTrue(user.id.isNotBlank())
        assertEquals("hashed-raw-password", user.passwordHash)
    }

    @Test
    fun `creating a user with an email already in use throws EmailAlreadyInUseException`() {
        every { userRepository.existsByEmail(any()) } answers { true }

        assertThrows<EmailAlreadyInUseException> {
            service(
                command = CreateUserCommand(
                    name = "new-name",
                    email = "new-email",
                    password = "raw-password",
                )
            )
        }
        verify(exactly = 0) { userRepository.save(any()) }
    }
}
