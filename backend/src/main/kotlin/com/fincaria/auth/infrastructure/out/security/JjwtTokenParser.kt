package com.fincaria.auth.infrastructure.out.security

import com.fincaria.auth.application.port.out.TokenParser
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component

@Component
class JjwtTokenParser(
    private val signingKey: JwtSigningKey,
) : TokenParser {
    override fun parse(token: String): String =
        Jwts.parser()
            .verifyWith(signingKey.value)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
}
