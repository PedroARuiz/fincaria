package com.fincaria.farms.infrastructure.out.outbox

import org.springframework.data.domain.Limit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

interface OutboxJpaRepository : JpaRepository<OutboxJpaEntity, UUID> {
    /** Pending events oldest first, capped to [limit] — the relay's read. */
    fun findByPublishedAtIsNullOrderByOccurredAtAsc(limit: Limit): List<OutboxJpaEntity>

    /**
     * Marks the given events as published in one bulk UPDATE. Annotated
     * `@Transactional` so it runs in its own short transaction when called from
     * the relay, keeping the mark separate from the (non-transactional) Kafka
     * publishing that precedes it.
     */
    @Modifying
    @Transactional
    @Query("UPDATE OutboxJpaEntity o SET o.publishedAt = :publishedAt WHERE o.id IN :ids")
    fun markPublished(@Param("ids") ids: Collection<UUID>, @Param("publishedAt") publishedAt: Instant)
}
