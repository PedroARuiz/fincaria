package com.fincaria.users.infrastructure.out.persistence

import com.fincaria.users.application.port.out.UserRepository
import com.fincaria.users.domain.User
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class UserRepositoryImpl : UserRepository {
    val users: ConcurrentHashMap<Long, User> = ConcurrentHashMap()
    fun create(user: User): User {
        val newUser = User(
            id = user.id,
            name= user.name,
            email = user.email,
        )
        users.put()
    }
}