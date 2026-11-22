package com.fincaria.farms.application.port.out

import com.fincaria.farms.domain.CropTypeCatalogEntry

/**
 * Outbound port to the crop type reference catalog. Returns every catalog
 * entry already ordered for presentation (by display order).
 *
 * Caching is an adapter concern and must not leak into this contract.
 */
interface CropTypeCatalogRepository {
    fun findAll(): List<CropTypeCatalogEntry>
}
