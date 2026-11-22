package com.fincaria.farms.application.service

import com.fincaria.farms.application.port.`in`.GetCropTypeCatalogUseCase
import com.fincaria.farms.application.port.out.CropTypeCatalogRepository
import com.fincaria.farms.domain.CropTypeCatalogEntry
import org.springframework.stereotype.Service

@Service
class GetCropTypeCatalogService(
    private val cropTypeCatalogRepository: CropTypeCatalogRepository,
) : GetCropTypeCatalogUseCase {
    override fun invoke(): List<CropTypeCatalogEntry> = cropTypeCatalogRepository.findAll()
}
