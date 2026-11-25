package com.fincaria.farms.infrastructure.out.outbox

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "outbox")
class OutboxJpaEntity(
    @Id
    var id: UUID,
    @Column(name = "aggregate_type", nullable = false, length = 50)
    var aggregateType: String,
    @Column(name = "aggregate_id", nullable = false, length = 64)
    var aggregateId: String,
    @Column(name = "event_type", nullable = false, length = 100)
    var eventType: String,
    // The already-serialized JSON document, bound to the Postgres `jsonb`
    // column via Hibernate's JSON type so the String is stored as real JSON
    // (queryable/inspectable) rather than an escaped text blob.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    var payload: String,
    @Column(name = "occurred_at", nullable = false)
    var occurredAt: Instant,
    // NULL until the relay publishes the event to the broker; set to the
    // publication time afterwards. This is the whole pending/published state.
    @Column(name = "published_at")
    var publishedAt: Instant? = null,
)
