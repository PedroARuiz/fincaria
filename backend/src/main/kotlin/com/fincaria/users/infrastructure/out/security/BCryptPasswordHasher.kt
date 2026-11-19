package com.fincaria.users.infrastructure.out.security

import com.fincaria.users.application.port.out.PasswordHasher
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class BCryptPasswordHasher : PasswordHasher {
    private val encoder = BCryptPasswordEncoder()

    override fun hash(rawPassword: String): String =
        encoder.encode(rawPassword) ?: error("BCryptPasswordEncoder returned a null hash, which should never happen")

    override fun matches(rawPassword: String, hashedPassword: String): Boolean =
        encoder.matches(rawPassword, hashedPassword)
}
