package com.fincaria.farms.infrastructure.out.cadastral

import com.fincaria.farms.application.port.out.CadastralRegistry
import com.fincaria.farms.domain.CadastralDetails
import com.fincaria.farms.domain.CadastralReference
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.retry.Retry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.function.Supplier

/**
 * [CadastralRegistry] adapter that wraps the external client with a retry and
 * a circuit breaker. The circuit breaker is the outermost decorator (retry
 * nested inside), so one logical lookup counts as a single call to the
 * breaker and an open circuit short-circuits without retrying.
 *
 * Any failure — retries exhausted or circuit open — degrades to `null`, which
 * the application interprets as "could not verify".
 */
@Component
class ResilientCadastralRegistry(
    private val client: SimulatedCadastralApiClient,
    private val cadastralCircuitBreaker: CircuitBreaker,
    private val cadastralRetry: Retry,
) : CadastralRegistry {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun lookup(reference: CadastralReference): CadastralDetails? {
        val decorated = CircuitBreaker.decorateSupplier(
            cadastralCircuitBreaker,
            Retry.decorateSupplier(cadastralRetry, Supplier { client.fetch(reference) }),
        )
        return try {
            decorated.get()
        } catch (e: CallNotPermittedException) {
            log.warn("Cadastral lookup short-circuited (circuit open) for {}: {}", reference.value, e.message)
            null
        } catch (e: CadastralApiException) {
            log.warn("Cadastral lookup failed after retries for {}: {}", reference.value, e.message)
            null
        }
    }
}
