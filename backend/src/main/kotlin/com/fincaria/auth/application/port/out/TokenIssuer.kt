package com.fincaria.auth.application.port.out

data class IssuedToken(
    val value: String,
    val expiresInSeconds: Long,
)

interface TokenIssuer {
    fun issue(userId: String): IssuedToken
}
