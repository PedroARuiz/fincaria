package com.fincaria.users.application.port.`in`

import com.fincaria.users.domain.User

data class CreateUserCommand(
    val name: String,
    val email: String,
    val password: String,
)

interface CreateUserUseCase {
    operator fun invoke(command: CreateUserCommand): User
}