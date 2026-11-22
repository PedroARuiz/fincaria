package com.fincaria.farms.infrastructure.out.persistence

import com.fincaria.farms.application.port.out.CropTypeCatalogRepository
import com.fincaria.farms.domain.CropTypeCatalogEntry
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@Repository
class CropTypeCatalogRepositoryImpl(
    private val cropTypeJpaRepository: CropTypeJpaRepository,
) : CropTypeCatalogRepository {
    // Cached here, at the infrastructure boundary, so caching never leaks into
    // the domain/application. The call comes from the use-case service (another
    // bean), so it goes through the Spring proxy and is intercepted.
    @Cacheable(CROP_TYPE_CATALOG_CACHE)
    override fun findAll(): List<CropTypeCatalogEntry> =
        cropTypeJpaRepository.findAllByOrderByDisplayOrderAsc().map { it.toDomain() }

    private fun CropTypeJpaEntity.toDomain(): CropTypeCatalogEntry =
        CropTypeCatalogEntry(
            code = code,
            label = label,
            description = description,
            displayOrder = displayOrder,
        )

    companion object {
        const val CROP_TYPE_CATALOG_CACHE = "cropTypeCatalog"
    }
}
