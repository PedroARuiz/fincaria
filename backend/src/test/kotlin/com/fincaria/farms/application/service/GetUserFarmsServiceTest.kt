package com.fincaria.farms.application.service

import com.fincaria.farms.application.port.out.FarmRepository
import com.fincaria.farms.domain.CadastralReference
import com.fincaria.farms.domain.CadastralVerification
import com.fincaria.farms.domain.CropType
import com.fincaria.farms.domain.Farm
import com.fincaria.shared.application.PageRequest
import com.fincaria.shared.application.SortDirection
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class GetUserFarmsServiceTest {
    val farmRepository: FarmRepository = mockk()
    val service = GetUserFarmsService(farmRepository = farmRepository)

    @Test
    fun `getting farms for a user returns the farms from the repository`() {
        val userId = UUID.randomUUID().toString()
        val expectedFarm = farm(userId, "7837301VG8173B0001TT")
        every { farmRepository.getUserFarms(userId) } returns listOf(expectedFarm)

        val result = service(userId = userId)

        assertEquals(listOf(expectedFarm), result.content)
        assertEquals(1L, result.totalElements)
    }

    @Test
    fun `getting farms for a user filtered by crop type returns only matching farms`() {
        val userId = UUID.randomUUID().toString()
        val matchingFarm = farm(userId, "7837301VG8173B0001TT")
        every { farmRepository.getUserFarms(userId) } returns listOf(matchingFarm)

        val result = service(userId = userId, cropType = CropType.OLIVE)

        assertEquals(listOf(matchingFarm), result.content)
    }

    @Test
    fun `getting farms for a user paginates the results`() {
        val userId = UUID.randomUUID().toString()
        val farms = listOf(
            farm(userId, "7837301VG8173B0001TT"),
            farm(userId, "4A08169P03PRAT0001LR"),
            farm(userId, "1234567AB1234C0002PD"),
        )
        every { farmRepository.getUserFarms(userId) } returns farms

        val firstPage = service(userId = userId, pageRequest = PageRequest(page = 0, size = 2))
        val secondPage = service(userId = userId, pageRequest = PageRequest(page = 1, size = 2))

        assertEquals(2, firstPage.content.size)
        assertEquals(1, secondPage.content.size)
        assertEquals(3L, firstPage.totalElements)
        assertEquals(2, firstPage.totalPages)
    }

    @Test
    fun `getting farms for a user sorts by cadastral reference`() {
        val userId = UUID.randomUUID().toString()
        val farmA = farm(userId, "1234567AB1234C0002PD")
        val farmB = farm(userId, "7837301VG8173B0001TT")
        every { farmRepository.getUserFarms(userId) } returns listOf(farmB, farmA)

        val ascending = service(
            userId = userId,
            pageRequest = PageRequest(page = 0, size = 10, sortBy = "cadastralReference"),
        )
        val descending = service(
            userId = userId,
            pageRequest = PageRequest(page = 0, size = 10, sortBy = "cadastralReference", sortDirection = SortDirection.DESC),
        )

        assertEquals(listOf(farmA, farmB), ascending.content)
        assertEquals(listOf(farmB, farmA), descending.content)
    }

    @Test
    fun `getting farms for a user with an unsupported sort field throws IllegalArgumentException`() {
        val userId = UUID.randomUUID().toString()
        every { farmRepository.getUserFarms(userId) } returns emptyList()

        assertThrows<IllegalArgumentException> {
            service(userId = userId, pageRequest = PageRequest(page = 0, size = 10, sortBy = "unknown"))
        }
    }

    private fun farm(userId: String, cadastralReference: String) = Farm(
        id = UUID.randomUUID().toString(),
        userId = userId,
        cadastralReference = CadastralReference(cadastralReference),
        cropTypes = setOf(CropType.OLIVE),
        cadastralVerification = CadastralVerification.Unverified,
    )
}
