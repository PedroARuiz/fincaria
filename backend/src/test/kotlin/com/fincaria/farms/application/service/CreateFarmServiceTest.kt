package com.fincaria.farms.application.service

import com.fincaria.farms.application.port.`in`.CreateFarmCommand
import com.fincaria.farms.application.port.out.CadastralRegistry
import com.fincaria.farms.application.port.out.FarmRepository
import com.fincaria.farms.domain.CadastralDetails
import com.fincaria.farms.domain.CadastralReference
import com.fincaria.farms.domain.CadastralVerification
import com.fincaria.farms.domain.CropType
import com.fincaria.farms.domain.DomainEvent
import com.fincaria.farms.domain.FarmRegistered
import com.fincaria.farms.domain.exception.DuplicatedCadastralReferenceException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class CreateFarmServiceTest {

    val farmRepository: FarmRepository = mockk()
    val cadastralRegistry: CadastralRegistry = mockk()
    val farmWriter: FarmWriter = mockk()
    val service = CreateFarmService(
        farmRepository = farmRepository,
        cadastralRegistry = cadastralRegistry,
        farmWriter = farmWriter,
    )

    @BeforeEach
    fun setup() {
        every { farmWriter.save(any(), any()) } answers { firstArg() }
        every { farmRepository.existsByCadastralReference(cadastralReference) } answers { false }
        every { cadastralRegistry.lookup(cadastralReference) } returns cadastralDetails
    }

    @Test
    fun `creating a farm with a cadastral reference not in use saves and returns the farm`() {
        val farm = service(command = command)

        assertEquals(cadastralReference.value, farm.cadastralReference.value)
    }

    @Test
    fun `creating a farm with a successful cadastral lookup marks it verified with the details`() {
        val farm = service(command = command)

        assertEquals(CadastralVerification.Verified(cadastralDetails), farm.cadastralVerification)
    }

    @Test
    fun `creating a farm when the cadastral lookup is unavailable degrades to an unverified farm`() {
        every { cadastralRegistry.lookup(cadastralReference) } returns null

        val farm = service(command = command)

        assertEquals(CadastralVerification.Unverified, farm.cadastralVerification)
        verify { farmWriter.save(any(), any()) }
    }

    @Test
    fun `creating a farm records a FarmRegistered event for the saved farm`() {
        val eventSlot = slot<DomainEvent>()
        every { farmWriter.save(any(), capture(eventSlot)) } answers { firstArg() }

        val farm = service(command = command)

        val event = assertInstanceOf(FarmRegistered::class.java, eventSlot.captured)
        assertEquals(farm.id, event.farmId)
        assertEquals(command.userId, event.userId)
        assertEquals(cadastralReference.value, event.cadastralReference)
        assertEquals(command.cropTypes, event.cropTypes)
    }

    @Test
    fun `creating a farm with a cadastral reference already in use throws DuplicatedCadastralReferenceException`() {
        every { farmRepository.existsByCadastralReference(cadastralReference) } answers { true }

        assertThrows<DuplicatedCadastralReferenceException> {
            service(command = command)
        }
        verify(exactly = 0) { farmWriter.save(any(), any()) }
    }

    @Test
    fun `creating a farm with no crop types throws IllegalArgumentException and does not save`() {
        val commandWithNoCropTypes = command.copy(cropTypes = emptySet())

        assertThrows<IllegalArgumentException> {
            service(command = commandWithNoCropTypes)
        }
        verify(exactly = 0) { farmWriter.save(any(), any()) }
    }

    companion object {
        val cadastralReference = CadastralReference("7837301VG8173B0001TT")
        val cadastralDetails = CadastralDetails(municipality = "Town", province = "Province", areaHectares = 12.5)
        val command = CreateFarmCommand(
            userId = UUID.randomUUID().toString(),
            cadastralReference = cadastralReference,
            cropTypes = setOf(CropType.OLIVE),
        )
    }
}
