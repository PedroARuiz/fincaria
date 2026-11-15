package com.fincaria.farms.infrastructure.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FarmJpaRepository : JpaRepository<FarmJpaEntity, UUID> {
    fun existsByCadastralReference(cadastralReference: String): Boolean
    fun findByUserId(userId: UUID): List<FarmJpaEntity>
}