package com.fincaria.farms.infrastructure.out.outbox

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OutboxJpaRepository : JpaRepository<OutboxJpaEntity, UUID>
