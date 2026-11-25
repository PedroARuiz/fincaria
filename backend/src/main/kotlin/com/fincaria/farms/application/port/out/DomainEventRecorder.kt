package com.fincaria.farms.application.port.out

import com.fincaria.farms.domain.DomainEvent

/**
 * Outbound port for recording a domain event so it will be published
 * downstream. Implementations persist the event (the outbox pattern's atomic
 * write) and must be called inside the same transaction as the state change
 * that produced it, so the business row and the event commit or roll back
 * together. Actual publication to the broker is a separate, asynchronous
 * concern handled by the relay.
 */
interface DomainEventRecorder {
    fun record(event: DomainEvent)
}
