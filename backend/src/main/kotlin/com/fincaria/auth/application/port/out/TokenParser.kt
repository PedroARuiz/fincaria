package com.fincaria.auth.application.port.out

interface TokenParser {
    /**
     * Parses and verifies the token, returning the userId (the `sub` claim).
     * Throws if the token is missing, malformed, has an invalid signature, or has expired.
     */
    fun parse(token: String): String
}
