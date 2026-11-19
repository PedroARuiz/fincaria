package com.fincaria.auth.infrastructure.`in`.web

import com.fincaria.auth.application.port.`in`.LoginCommand
import com.fincaria.auth.application.port.`in`.LoginUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val loginUseCase: LoginUseCase,
) {
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val command = LoginCommand(email = request.email, password = request.password)
        val issuedToken = loginUseCase(command)
        val response = LoginResponse(token = issuedToken.value, expiresInSeconds = issuedToken.expiresInSeconds)
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }
}
