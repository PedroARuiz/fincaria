package com.fincaria.farms.application.service

import com.fincaria.farms.application.port.out.FarmRepository
import com.fincaria.farms.domain.CadastralReference
import com.fincaria.farms.domain.CadastralVerification
import com.fincaria.farms.domain.CropType
import com.fincaria.farms.domain.Farm
import com.fincaria.shared.application.PagedResult
import com.fincaria.shared.application.PageRequest
import com.fincaria.shared.application.SortDirection
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class GetUserFarmsServiceTest {
    private val farmRepository: FarmRepository = mockk()
    private val service = GetUserFarmsService(farmRepository = farmRepository)

    @Test
    fun `delegates to the repository with the given filter and page request`() {
        val userId = UUID.randomUUID().toString()
        val pageRequest = PageRequest(page = 1, size = 5, sortBy = "cadastralReference", sortDirection = SortDirection.DESC)
        val expected = PagedResult(
            content = listOf(farm(userId)),
            page = 1,
            size = 5,
            totalElements = 6L,
            totalPages = 2,
        )
        every { farmRepository.getUserFarms(userId, CropType.OLIVE, pageRequest) } returns expected

        val result = service(userId = userId, cropType = CropType.OLIVE, pageRequest = pageRequest)

        assertEquals(expected, result)
        verify(exactly = 1) { farmRepository.getUserFarms(userId, CropType.OLIVE, pageRequest) }
    }

    @Test
    fun `allows an unsorted request`() {
        val userId = UUID.randomUUID().toString()
        val pageRequest = PageRequest(page = 0, size = 10)
        val expected = PagedResult(content = emptyList<Farm>(), page = 0, size = 10, totalElements = 0L, totalPages = 0)
        every { farmRepository.getUserFarms(userId, null, pageRequest) } returns expected

        assertEquals(expected, service(userId = userId, pageRequest = pageRequest))
    }

    @Test
    fun `rejects an unsupported sort field without touching the repository`() {
        val userId = UUID.randomUUID().toString()

        assertThrows<IllegalArgumentException> {
            service(userId = userId, pageRequest = PageRequest(page = 0, size = 10, sortBy = "unknown"))
        }

        verify(exactly = 0) { farmRepository.getUserFarms(any(), any(), any()) }
    }

    private fun farm(userId: String) = Farm(
        id = UUID.randomUUID().toString(),
        userId = userId,
        cadastralReference = CadastralReference("7837301VG8173B0001TT"),
        cropTypes = setOf(CropType.OLIVE),
        cadastralVerification = CadastralVerification.Unverified,
    )
}
