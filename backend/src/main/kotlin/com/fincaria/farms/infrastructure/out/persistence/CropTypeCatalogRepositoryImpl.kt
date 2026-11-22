package com.fincaria.farms.infrastructure.out.persistence

import com.fincaria.farms.application.port.out.CropTypeCatalogRepository
import com.fincaria.farms.domain.CropTypeCatalogEntry
import org.springframework.stereotype.Repository

@Repository
class CropTypeCatalogRepositoryImpl(
    private val cropTypeJpaRepository: CropTypeJpaRepository,
) : CropTypeCatalogRepository {
    override fun findAll(): List<CropTypeCatalogEntry> =
        cropTypeJpaRepository.findAllByOrderByDisplayOrderAsc().map { it.toDomain() }

    private fun CropTypeJpaEntity.toDomain(): CropTypeCatalogEntry =
        CropTypeCatalogEntry(
            code = code,
            label = label,
            description = description,
            displayOrder = displayOrder,
        )
}
