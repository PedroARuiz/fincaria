package com.fincaria.users.application.port.out

interface PasswordHasher {
    fun hash(rawPassword: String): String

    fun matches(rawPassword: String, hashedPassword: String): Boolean
}
