package com.fincaria.auth.application.port.`in`

import com.fincaria.auth.application.port.out.IssuedToken

data class LoginCommand(
    val email: String,
    val password: String,
)

interface LoginUseCase {
    operator fun invoke(command: LoginCommand): IssuedToken
}
