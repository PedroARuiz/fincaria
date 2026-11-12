package com.fincaria.users.infrastructure.out.persistence

import com.fincaria.users.application.port.out.UserRepository
import com.fincaria.users.domain.User
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepositoryImpl(
    private val userJpaRepository: UserJpaRepository,
) : UserRepository {
    override fun save(user: User): User {
        val entity = UserJpaEntity(
            id = UUID.fromString(user.id),
            name = user.name,
            email = user.email,
        )
        val saved = userJpaRepository.save(entity)
        return User(id = saved.id.toString(), name = saved.name, email = saved.email)
    }

    override fun existsByEmail(email: String): Boolean =
        userJpaRepository.existsByEmail(email)
}