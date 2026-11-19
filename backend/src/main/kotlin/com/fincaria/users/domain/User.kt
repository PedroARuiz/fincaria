package com.fincaria.users.domain

data class User(
    val id: String,
    val email: String,
    val name: String,
    val passwordHash: String,
)
