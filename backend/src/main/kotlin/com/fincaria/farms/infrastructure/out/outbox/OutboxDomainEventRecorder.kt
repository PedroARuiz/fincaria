package com.fincaria.farms.infrastructure.out.outbox

import com.fincaria.farms.application.port.out.DomainEventRecorder
import com.fincaria.farms.domain.DomainEvent
import com.fincaria.farms.domain.FarmRegistered
import org.springframework.stereotype.Repository
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.UUID

/**
 * Records domain events into the `outbox` table. This adapter is the single
 * place where the wire contract lives: it maps each domain event to its
 * aggregate/event type labels and serializes a stable JSON payload, keeping
 * that representation out of the domain. Adding a new event means adding a
 * branch here, which makes any change to the published contract a deliberate,
 * reviewable one.
 */
@Repository
class OutboxDomainEventRecorder(
    private val outboxJpaRepository: OutboxJpaRepository,
    private val objectMapper: ObjectMapper,
) : DomainEventRecorder {
    override fun record(event: DomainEvent) {
        val entity = when (event) {
            is FarmRegistered -> event.toOutboxEntity()
            else -> throw IllegalArgumentException(
                "Unsupported domain event: ${event::class.qualifiedName}",
            )
        }
        outboxJpaRepository.save(entity)
    }

    private fun FarmRegistered.toOutboxEntity(): OutboxJpaEntity =
        OutboxJpaEntity(
            id = UUID.randomUUID(),
            aggregateType = AGGREGATE_TYPE_FARM,
            aggregateId = aggregateId,
            eventType = EVENT_TYPE_FARM_REGISTERED,
            payload = objectMapper.writeValueAsString(
                FarmRegisteredPayload(
                    farmId = farmId,
                    userId = userId,
                    cadastralReference = cadastralReference,
                    cropTypes = cropTypes.map { it.name }.toSet(),
                ),
            ),
            occurredAt = occurredAt,
        )

    /**
     * Explicit, primitive-typed JSON shape of the published event. Decoupled
     * from the domain class so renaming a domain field never silently changes
     * the contract consumers rely on.
     */
    private data class FarmRegisteredPayload(
        val farmId: String,
        val userId: String,
        val cadastralReference: String,
        val cropTypes: Set<String>,
    )

    private companion object {
        const val AGGREGATE_TYPE_FARM = "Farm"
        const val EVENT_TYPE_FARM_REGISTERED = "FarmRegistered"
    }
}
