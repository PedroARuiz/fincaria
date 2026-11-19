package com.fincaria.auth.application.service

import com.fincaria.auth.application.port.`in`.LoginCommand
import com.fincaria.auth.application.port.`in`.LoginUseCase
import com.fincaria.auth.application.port.out.IssuedToken
import com.fincaria.auth.application.port.out.TokenIssuer
import com.fincaria.auth.domain.exception.InvalidCredentialsException
import com.fincaria.users.application.port.out.PasswordHasher
import com.fincaria.users.application.port.out.UserRepository
import org.springframework.stereotype.Service

@Service
class LoginService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenIssuer: TokenIssuer,
) : LoginUseCase {
    override fun invoke(command: LoginCommand): IssuedToken {
        val user = userRepository.findByEmail(command.email) ?: throw InvalidCredentialsException()
        if (!passwordHasher.matches(command.password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }
        return tokenIssuer.issue(user.id)
    }
}
