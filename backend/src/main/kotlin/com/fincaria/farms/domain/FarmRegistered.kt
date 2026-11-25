package com.fincaria.farms.domain

import java.time.Instant

/**
 * Emitted when a farm has been registered. Carries the facts a downstream
 * consumer needs (e.g. the future crop-history context initializing an empty
 * production log) without exposing any farm internals beyond them.
 */
data class FarmRegistered(
    val farmId: String,
    val userId: String,
    val cadastralReference: String,
    val cropTypes: Set<CropType>,
    override val occurredAt: Instant,
) : DomainEvent {
    override val aggregateId: String get() = farmId
}
