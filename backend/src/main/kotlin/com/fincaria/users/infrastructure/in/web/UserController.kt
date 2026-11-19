package com.fincaria.users.infrastructure.`in`.web

import com.fincaria.users.application.port.`in`.CreateUserCommand
import com.fincaria.users.application.port.`in`.CreateUserUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val createUserUseCase: CreateUserUseCase,
) {
    @PostMapping
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<CreateUserResponse> {
        val command = CreateUserCommand(name = request.name, email = request.email, password = request.password)
        val user = createUserUseCase(command)
        val response = CreateUserResponse(id = user.id, name = user.name, email = user.email)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}