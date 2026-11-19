package com.fincaria.auth.infrastructure.out.security

import com.fincaria.auth.application.port.out.IssuedToken
import com.fincaria.auth.application.port.out.TokenIssuer
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date

@Component
class JjwtTokenIssuer(
    @Value("\${fincaria.jwt.secret}") secret: String,
    @Value("\${fincaria.jwt.expiration-seconds}") private val expirationSeconds: Long,
) : TokenIssuer {
    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    override fun issue(userId: String): IssuedToken {
        val now = Instant.now()
        val token = Jwts.builder()
            .subject(userId)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(expirationSeconds)))
            .signWith(key)
            .compact()
        return IssuedToken(value = token, expiresInSeconds = expirationSeconds)
    }
}
