package com.fincaria.users.application.service

import com.fincaria.users.application.port.`in`.CreateUserUseCase
import com.fincaria.users.application.port.out.UserRepository
import org.springframework.stereotype.Service

@Service
class CreateUserService(
    private val userRepository: UserRepository,
) : CreateUserUseCase {
}