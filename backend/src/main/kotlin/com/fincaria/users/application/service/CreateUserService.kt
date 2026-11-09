package com.fincaria.users.application.service

import com.fincaria.users.application.port.`in`.CreateUserCommand
import com.fincaria.users.application.port.`in`.CreateUserUseCase
import com.fincaria.users.application.port.out.UserRepository
import com.fincaria.users.domain.User
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CreateUserService(
    private val userRepository: UserRepository,
) : CreateUserUseCase {
    override fun execute(command: CreateUserCommand): User {
        val user = User(id = UUID.randomUUID().toString(), email = command.email, name = command.name)
        return userRepository.save(user)

    }
}