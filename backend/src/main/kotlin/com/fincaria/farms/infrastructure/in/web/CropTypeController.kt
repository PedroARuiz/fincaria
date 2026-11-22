package com.fincaria.farms.infrastructure.`in`.web

import com.fincaria.farms.application.port.`in`.GetCropTypeCatalogUseCase
import com.fincaria.farms.domain.CropTypeCatalogEntry
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CropTypeController(
    private val getCropTypeCatalogUseCase: GetCropTypeCatalogUseCase,
) {
    @GetMapping("/api/v1/crop-types")
    fun getCropTypeCatalog(): ResponseEntity<List<CropTypeResponse>> {
        val catalog = getCropTypeCatalogUseCase().map { it.toResponse() }
        return ResponseEntity.status(HttpStatus.OK).body(catalog)
    }

    private fun CropTypeCatalogEntry.toResponse(): CropTypeResponse =
        CropTypeResponse(
            code = code.name,
            label = label,
            description = description,
        )
}
