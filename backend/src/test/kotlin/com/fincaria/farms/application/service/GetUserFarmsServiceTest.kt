package com.fincaria.farms.application.service

import com.fincaria.farms.application.port.out.FarmRepository
import com.fincaria.farms.domain.CadastralReference
import com.fincaria.farms.domain.CropType
import com.fincaria.farms.domain.Farm
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class GetUserFarmsServiceTest {
    val farmRepository: FarmRepository = mockk()
    val service = GetUserFarmsService(farmRepository = farmRepository)

    @Test
    fun `getting farms for a user returns the farms from the repository`() {
        val userId = UUID.randomUUID().toString()
        val expectedFarms = listOf(
            Farm(
                id = UUID.randomUUID().toString(),
                userId = userId,
                cadastralReference = CadastralReference("7837301VG8173B0001TT"),
                cropTypes = setOf(CropType.OLIVE),
            )
        )
        every { farmRepository.getUserFarms(userId) } returns expectedFarms

        val farms = service(userId = userId)

        assertEquals(expectedFarms, farms)
    }

    @Test
    fun `getting farms for a user filtered by crop type returns only matching farms`() {
        val userId = UUID.randomUUID().toString()
        val matchingFarm = Farm(
            id = UUID.randomUUID().toString(),
            userId = userId,
            cadastralReference = CadastralReference("7837301VG8173B0001TT"),
            cropTypes = setOf(CropType.OLIVE),
        )
        every { farmRepository.getUserFarms(userId) } returns listOf(matchingFarm)

        val farms = service(userId = userId, cropType = CropType.OLIVE)

        assertEquals(listOf(matchingFarm), farms)
    }
}
