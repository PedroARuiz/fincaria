package com.fincaria.auth.infrastructure.out.security

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.SignatureException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.Date

class JjwtTokenParserTest {

    private val signingKey = JwtSigningKey("test-only-jwt-secret-at-least-48-bytes-long-xxxxxx")
    private val issuer = JjwtTokenIssuer(signingKey = signingKey, expirationSeconds = 3600)
    private val parser = JjwtTokenParser(signingKey = signingKey)

    @Test
    fun `parsing a token issued by JjwtTokenIssuer returns its subject`() {
        val issuedToken = issuer.issue("user-id")

        assertEquals("user-id", parser.parse(issuedToken.value))
    }

    @Test
    fun `parsing a token signed with a different secret throws`() {
        val issuedToken = issuer.issue("user-id")
        val otherParser = JjwtTokenParser(JwtSigningKey("another-test-jwt-secret-at-least-48-bytes-long-yyyy"))

        assertThrows<SignatureException> { otherParser.parse(issuedToken.value) }
    }

    @Test
    fun `parsing an expired token throws`() {
        val now = Instant.now()
        val expiredToken = Jwts.builder()
            .subject("user-id")
            .issuedAt(Date.from(now.minusSeconds(120)))
            .expiration(Date.from(now.minusSeconds(60)))
            .signWith(signingKey.value)
            .compact()

        assertThrows<ExpiredJwtException> { parser.parse(expiredToken) }
    }

    @Test
    fun `parsing a malformed string throws`() {
        assertThrows<Exception> { parser.parse("not-a-jwt") }
    }
}
