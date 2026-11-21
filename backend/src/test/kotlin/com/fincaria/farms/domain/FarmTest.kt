package com.fincaria.farms.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class FarmTest {

    @Test
    fun `creating a farm with no crop types throws IllegalArgumentException`() {
        assertThrows<IllegalArgumentException> {
            Farm(
                id = UUID.randomUUID().toString(),
                userId = UUID.randomUUID().toString(),
                cadastralReference = CadastralReference("7837301VG8173B0001TT"),
                cropTypes = emptySet(),
                cadastralVerification = CadastralVerification.Unverified,
            )
        }
    }

    @Test
    fun `creating a farm with at least one crop type succeeds`() {
        val farm = Farm(
            id = UUID.randomUUID().toString(),
            userId = UUID.randomUUID().toString(),
            cadastralReference = CadastralReference("7837301VG8173B0001TT"),
            cropTypes = setOf(CropType.OLIVE),
            cadastralVerification = CadastralVerification.Unverified,
        )

        assertEquals(setOf(CropType.OLIVE), farm.cropTypes)
    }
}
