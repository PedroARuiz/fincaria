package com.fincaria.farms.infrastructure.out.outbox

import com.fincaria.farms.domain.CropType
import com.fincaria.farms.domain.FarmRegistered
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.util.UUID

/**
 * Pins the published JSON contract of the FarmRegistered event (ADR-0008).
 * Exercised at the serialization boundary (the recorder), before the payload
 * round-trips through the jsonb column, so the exact shape is asserted.
 *
 * Renaming, removing or retyping a field is an incompatible contract change
 * (not allowed without versioning the event) and must break this test — it is
 * the lightweight stand-in for a Schema Registry's compatibility check.
 */
class FarmRegisteredContractTest {

    private val outboxJpaRepository: OutboxJpaRepository = mockk()
    private val objectMapper: ObjectMapper = JsonMapper.builder().build()
    private val recorder = OutboxDomainEventRecorder(outboxJpaRepository, objectMapper)

    @Test
    fun `FarmRegistered is published with exactly its contract fields`() {
        val savedEntity = slot<OutboxJpaEntity>()
        every { outboxJpaRepository.save(capture(savedEntity)) } answers { firstArg() }

        val farmId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()
        recorder.record(
            FarmRegistered(
                farmId = farmId,
                userId = userId,
                cadastralReference = "7837301VG8173B0001TT",
                cropTypes = setOf(CropType.OLIVE),
                occurredAt = Instant.parse("2026-01-01T00:00:00Z"),
            ),
        )

        val entity = savedEntity.captured
        assertThat(entity.aggregateType).isEqualTo("Farm")
        assertThat(entity.eventType).isEqualTo("FarmRegistered")
        assertThat(entity.aggregateId).isEqualTo(farmId)

        val payload = objectMapper.readValue(entity.payload, Map::class.java)
        // Exact field set: fails if a field is added, renamed or removed.
        assertThat(payload.keys).containsExactlyInAnyOrder("farmId", "userId", "cadastralReference", "cropTypes")
        assertThat(payload["farmId"]).isEqualTo(farmId)
        assertThat(payload["userId"]).isEqualTo(userId)
        assertThat(payload["cadastralReference"]).isEqualTo("7837301VG8173B0001TT")
        assertThat(payload["cropTypes"]).isEqualTo(listOf("OLIVE"))
    }
}
