package com.fincaria.farms.application.port.`in`

import com.fincaria.farms.domain.CropTypeCatalogEntry

interface GetCropTypeCatalogUseCase {
    operator fun invoke(): List<CropTypeCatalogEntry>
}
