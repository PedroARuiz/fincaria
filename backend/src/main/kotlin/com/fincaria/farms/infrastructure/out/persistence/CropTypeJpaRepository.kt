package com.fincaria.farms.infrastructure.out.persistence

import com.fincaria.farms.domain.CropType
import org.springframework.data.jpa.repository.JpaRepository

interface CropTypeJpaRepository : JpaRepository<CropTypeJpaEntity, CropType> {
    fun findAllByOrderByDisplayOrderAsc(): List<CropTypeJpaEntity>
}
