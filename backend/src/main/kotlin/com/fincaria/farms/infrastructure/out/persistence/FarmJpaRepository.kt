package com.fincaria.farms.infrastructure.out.persistence

import com.fincaria.farms.domain.CropType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface FarmJpaRepository : JpaRepository<FarmJpaEntity, UUID> {
    fun existsByCadastralReference(cadastralReference: String): Boolean

    fun findByUserId(userId: UUID, pageable: Pageable): Page<FarmJpaEntity>

    @Query("SELECT f FROM FarmJpaEntity f WHERE f.userId = :userId AND :cropType MEMBER OF f.cropTypes")
    fun findByUserIdAndCropType(
        @Param("userId") userId: UUID,
        @Param("cropType") cropType: CropType,
        pageable: Pageable,
    ): Page<FarmJpaEntity>
}
