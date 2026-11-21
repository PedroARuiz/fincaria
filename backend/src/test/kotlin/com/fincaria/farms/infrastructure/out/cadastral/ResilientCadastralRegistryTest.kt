package com.fincaria.farms.infrastructure.out.cadastral

import com.fincaria.farms.domain.CadastralDetails
import com.fincaria.farms.domain.CadastralReference
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Duration

class ResilientCadastralRegistryTest {

    private val client: SimulatedCadastralApiClient = mockk()

    // Small, fast test configuration: 2 retry attempts per lookup, and a circuit
    // that opens after 2 fully-failed lookups (100% > 50% over a 2-call window).
    private fun newRegistry(): ResilientCadastralRegistry {
        val circuitBreaker = CircuitBreaker.of(
            "test",
            CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(2)
                .minimumNumberOfCalls(2)
                .failureRateThreshold(50f)
                .waitDurationInOpenState(Duration.ofMinutes(1))
                .build(),
        )
        val retry = Retry.of(
            "test",
            RetryConfig.custom<Any>()
                .maxAttempts(2)
                .waitDuration(Duration.ofMillis(1))
                .retryExceptions(CadastralApiException::class.java)
                .build(),
        )
        return ResilientCadastralRegistry(client, circuitBreaker, retry)
    }

    @Test
    fun `a successful call returns the details without retrying`() {
        every { client.fetch(reference) } returns details

        val result = newRegistry().lookup(reference)

        assertEquals(details, result)
        verify(exactly = 1) { client.fetch(reference) }
    }

    @Test
    fun `a failing call is retried and then falls back to null`() {
        every { client.fetch(reference) } throws CadastralApiException("down")

        val result = newRegistry().lookup(reference)

        assertNull(result)
        verify(exactly = 2) { client.fetch(reference) }
    }

    @Test
    fun `repeated failures open the circuit and short-circuit later calls`() {
        every { client.fetch(reference) } throws CadastralApiException("down")
        val registry = newRegistry()

        // Two failed lookups (2 attempts each) fill the window and open the circuit.
        registry.lookup(reference)
        registry.lookup(reference)

        // The circuit is now open: this lookup returns the fallback without hitting the client.
        val result = registry.lookup(reference)

        assertNull(result)
        verify(exactly = 4) { client.fetch(reference) }
    }

    companion object {
        val reference = CadastralReference("7837301VG8173B0001TT")
        val details = CadastralDetails(municipality = "Town", province = "Province", areaHectares = 12.5)
    }
}
