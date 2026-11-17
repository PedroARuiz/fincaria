package com.fincaria.users.application.service

import com.fincaria.users.application.port.`in`.CreateUserCommand
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
    val service = CreateUserService(userRepository = userRepository)

    @BeforeEach
    fun setup() {
        every { userRepository.save(any()) } answers { firstArg() }
        every { userRepository.existsByEmail(any()) } answers { false }
    }

    @Test
    fun `creating a user with a new email saves and returns the user`() {
        val command = CreateUserCommand(
            name = "new-name",
            email = "new-email",
        )

        val user = service(command = command)

        assertEquals(command.name, user.name)
        assertEquals(command.email, user.email)
        assertTrue(user.id.isNotBlank())
    }

    @Test
    fun `creating a user with an email already in use throws EmailAlreadyInUseException`() {
        every { userRepository.existsByEmail(any()) } answers { true }

        assertThrows<EmailAlreadyInUseException> {
            service(
                command = CreateUserCommand(
                    name = "new-name",
                    email = "new-email",
                )
            )
        }
        verify(exactly = 0) { userRepository.save(any()) }
    }
}
