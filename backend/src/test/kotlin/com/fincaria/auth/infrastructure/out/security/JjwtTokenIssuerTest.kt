package com.fincaria.auth.infrastructure.out.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class JjwtTokenIssuerTest {

    private val secret = "test-only-jwt-secret-at-least-48-bytes-long-xxxxxx"
    private val issuer = JjwtTokenIssuer(signingKey = JwtSigningKey(secret), expirationSeconds = 3600)

    @Test
    fun `issuing a token sets the subject and an expiration matching the configured seconds`() {
        val issuedToken = issuer.issue("user-id")

        val claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.toByteArray()))
            .build()
            .parseSignedClaims(issuedToken.value)
            .payload

        assertEquals("user-id", claims.subject)
        assertEquals(3600, issuedToken.expiresInSeconds)
        val secondsUntilExpiration = (claims.expiration.time - claims.issuedAt.time) / 1000
        assertTrue(secondsUntilExpiration in 3590..3600)
    }

    @Test
    fun `a token signed with a different secret fails verification`() {
        val issuedToken = issuer.issue("user-id")
        val otherSecret = "another-test-jwt-secret-at-least-48-bytes-long-yyyy"

        assertTrue(
            runCatching {
                Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(otherSecret.toByteArray()))
                    .build()
                    .parseSignedClaims(issuedToken.value)
            }.isFailure
        )
    }
}
