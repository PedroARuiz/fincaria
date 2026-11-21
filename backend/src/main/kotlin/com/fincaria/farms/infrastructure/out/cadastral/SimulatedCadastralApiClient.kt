package com.fincaria.farms.infrastructure.out.cadastral

import com.fincaria.farms.domain.CadastralDetails
import com.fincaria.farms.domain.CadastralReference
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import kotlin.math.absoluteValue
import kotlin.random.Random

/**
 * Stand-in for a real external cadastral registry API (e.g. the Spanish
 * Catastro OVC service). It fabricates deterministic details for a given
 * reference, and can be made to fail on a configurable fraction of calls via
 * `fincaria.cadastral.failure-rate` (0.0 by default) so that retry and circuit
 * breaker behaviour can be exercised and demoed locally.
 */
@Component
class SimulatedCadastralApiClient(
    @Value("\${fincaria.cadastral.failure-rate:0.0}") private val failureRate: Double,
) {
    fun fetch(reference: CadastralReference): CadastralDetails {
        if (Random.nextDouble() < failureRate) {
            throw CadastralApiException("Simulated cadastral registry outage for ${reference.value}")
        }
        // Deterministic fake data derived from the reference — this is a simulation.
        return CadastralDetails(
            municipality = "Simulated municipality ${reference.value.take(5)}",
            province = "Simulated province ${reference.value.take(2)}",
            areaHectares = (reference.value.hashCode().absoluteValue % 10_000) / 100.0,
        )
    }
}
