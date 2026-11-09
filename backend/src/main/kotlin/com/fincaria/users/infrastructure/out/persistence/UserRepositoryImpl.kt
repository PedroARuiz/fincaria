package com.fincaria.users.infrastructure.out.persistence

import com.fincaria.users.application.port.out.UserRepository
import com.fincaria.users.domain.User
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class UserRepositoryImpl : UserRepository {
    val users: ConcurrentHashMap<String, User> = ConcurrentHashMap()
    override fun save(user: User): User = user.also {
        users[user.id] = it
    }

    override fun existsByEmail(email: String): Boolean =
        users.filterValues { it.email == email }.isNotEmpty()
}