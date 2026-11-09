package com.fincaria.users.application.port.out

import com.fincaria.users.domain.User

interface UserRepository {
    fun save(user: User): User
}