package com.fincaria.farms.application.service

import com.fincaria.farms.application.port.out.DomainEventRecorder
import com.fincaria.farms.application.port.out.FarmRepository
import com.fincaria.farms.domain.DomainEvent
import com.fincaria.farms.domain.Farm
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Persists a farm together with its domain event in a single database
 * transaction, so the farm row and the outbox row commit or roll back as one
 * (the outbox pattern's atomic write, which removes the farm/broker dual-write
 * problem).
 *
 * It lives in its own bean, separate from [CreateFarmService], on purpose:
 * Spring's `@Transactional` only applies when the method is called through the
 * proxy, i.e. from another bean. This lets [CreateFarmService] run the external
 * cadastral lookup *before* calling here, outside the transaction, instead of
 * holding a database connection open across a network call.
 */
@Service
class FarmWriter(
    private val farmRepository: FarmRepository,
    private val domainEventRecorder: DomainEventRecorder,
) {
    @Transactional
    fun save(farm: Farm, event: DomainEvent): Farm {
        val savedFarm = farmRepository.save(farm)
        domainEventRecorder.record(event)
        return savedFarm
    }
}
