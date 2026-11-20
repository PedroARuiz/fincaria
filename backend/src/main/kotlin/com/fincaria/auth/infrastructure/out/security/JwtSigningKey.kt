package com.fincaria.auth.infrastructure.out.security

import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.crypto.SecretKey

@Component
class JwtSigningKey(
    @Value("\${fincaria.jwt.secret}") secret: String,
) {
    val value: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())
}
