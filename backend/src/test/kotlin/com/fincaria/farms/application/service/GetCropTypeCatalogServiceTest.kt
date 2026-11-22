package com.fincaria.farms.application.service

import com.fincaria.farms.application.port.out.CropTypeCatalogRepository
import com.fincaria.farms.domain.CropType
import com.fincaria.farms.domain.CropTypeCatalogEntry
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetCropTypeCatalogServiceTest {
    private val cropTypeCatalogRepository: CropTypeCatalogRepository = mockk()
    private val service = GetCropTypeCatalogService(cropTypeCatalogRepository = cropTypeCatalogRepository)

    @Test
    fun `getting the catalog returns the entries from the repository`() {
        val entries = listOf(
            CropTypeCatalogEntry(
                code = CropType.OLIVE,
                label = "Olive grove",
                description = "Olive tree cultivation.",
                displayOrder = 1,
            ),
        )
        every { cropTypeCatalogRepository.findAll() } returns entries

        val result = service()

        assertEquals(entries, result)
    }
}
