package com.fincaria.farms.domain

import java.time.Instant

/**
 * A fact that has happened in the domain and may trigger downstream reactions.
 *
 * Kept deliberately free of any transport or persistence concern (no Kafka,
 * JSON or JPA here): how an event is stored in the outbox and published to the
 * broker is decided at the infrastructure boundary, not by the domain.
 */
interface DomainEvent {
    /** Identity of the aggregate this event is about (e.g. the farm id). */
    val aggregateId: String

    /** When the fact occurred. */
    val occurredAt: Instant
}
