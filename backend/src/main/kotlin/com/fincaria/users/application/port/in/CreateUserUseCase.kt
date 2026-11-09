package com.fincaria.users.application.port.`in`

import com.fincaria.users.domain.User

data class CreateUserCommand(
    val name: String,
    val email: String,
)

interface CreateUserUseCase {
    fun execute(command: CreateUserCommand): User
}