package com.fincaria.farms.application.port.out

import com.fincaria.farms.domain.CadastralDetails
import com.fincaria.farms.domain.CadastralReference

/**
 * Outbound port to an external cadastral registry that can enrich a farm's
 * cadastral reference with extra data. Returns `null` when the reference
 * cannot be resolved — including when the registry is unavailable — so the
 * application can degrade gracefully instead of failing farm creation.
 *
 * Resilience (retry, circuit breaker) is an adapter concern and must not
 * leak into this contract.
 */
interface CadastralRegistry {
    fun lookup(reference: CadastralReference): CadastralDetails?
}
