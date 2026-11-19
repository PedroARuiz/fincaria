package com.fincaria.auth.infrastructure.`in`.web

data class LoginResponse(
    val token: String,
    val expiresInSeconds: Long,
)
