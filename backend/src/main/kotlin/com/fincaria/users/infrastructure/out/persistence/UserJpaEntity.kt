package com.fincaria.users.infrastructure.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "users")
class UserJpaEntity(
    @Id
    var id: UUID,
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false, unique = true, length = 254)
    var email: String,
    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,
)
