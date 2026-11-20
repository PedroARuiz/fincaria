package com.fincaria.auth.infrastructure.out.security

import com.fincaria.auth.application.port.out.IssuedToken
import com.fincaria.auth.application.port.out.TokenIssuer
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date

@Component
class JjwtTokenIssuer(
    private val signingKey: JwtSigningKey,
    @Value("\${fincaria.jwt.expiration-seconds}") private val expirationSeconds: Long,
) : TokenIssuer {
    override fun issue(userId: String): IssuedToken {
        val now = Instant.now()
        val token = Jwts.builder()
            .subject(userId)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(expirationSeconds)))
            .signWith(signingKey.value, Jwts.SIG.HS384)
            .compact()
        return IssuedToken(value = token, expiresInSeconds = expirationSeconds)
    }
}
