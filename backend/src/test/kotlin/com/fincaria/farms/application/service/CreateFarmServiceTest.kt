package com.fincaria.farms.application.service

import com.fincaria.farms.application.port.`in`.CreateFarmCommand
import com.fincaria.farms.application.port.out.FarmRepository
import com.fincaria.farms.domain.CadastralReference
import com.fincaria.farms.domain.CropType
import com.fincaria.farms.domain.exception.DuplicatedCadastralReferenceException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class CreateFarmServiceTest {

    val farmRepository: FarmRepository = mockk()
    val service = CreateFarmService(farmRepository = farmRepository)

    @BeforeEach
    fun setup() {
        every { farmRepository.save(any()) } answers { firstArg() }
        every {
            farmRepository.existsByCadastralReference(cadastralReference)
        } answers { false }
    }

    @Test
    fun `creating a farm with a cadastral reference not in use saves and returns the farm`() {
        val farm = service(command = command)

        assertEquals(cadastralReference.value, farm.cadastralReference.value)
    }

    @Test
    fun `creating a farm with a cadastral reference already in use throws DuplicatedCadastralReferenceException`() {
        every { farmRepository.existsByCadastralReference(cadastralReference) } answers { true }

        assertThrows<DuplicatedCadastralReferenceException> {
            service(command = command)
        }
        verify(exactly = 0) { farmRepository.save(any()) }
    }

    companion object {
        val cadastralReference = CadastralReference("7837301VG8173B0001TT")
        val command = CreateFarmCommand(
            userId = UUID.randomUUID().toString(),
            cadastralReference = cadastralReference,
            cropType = CropType.OLIVE,
        )
    }
}
