package com.fincaria.farms.infrastructure.out.cadastral

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

/**
 * Wires the Resilience4j circuit breaker and retry used by the cadastral
 * registry adapter, binds their metrics to the Micrometer/Prometheus registry,
 * and logs state transitions and retries.
 *
 * Resilience4j has no Spring Boot 4 starter yet (the published starter targets
 * Spring Framework 6), so the plain core modules are configured programmatically
 * here instead of via annotations/auto-configuration.
 */
@Configuration
class CadastralResilienceConfig {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun circuitBreakerRegistry(meterRegistry: MeterRegistry): CircuitBreakerRegistry {
        val config = CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .failureRateThreshold(50f)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .permittedNumberOfCallsInHalfOpenState(3)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .build()
        val registry = CircuitBreakerRegistry.of(config)
        TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(registry).bindTo(meterRegistry)
        return registry
    }

    @Bean
    fun retryRegistry(meterRegistry: MeterRegistry): RetryRegistry {
        val config = RetryConfig.custom<Any>()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(200))
            .retryExceptions(CadastralApiException::class.java)
            .build()
        val registry = RetryRegistry.of(config)
        TaggedRetryMetrics.ofRetryRegistry(registry).bindTo(meterRegistry)
        return registry
    }

    @Bean
    fun cadastralCircuitBreaker(circuitBreakerRegistry: CircuitBreakerRegistry): CircuitBreaker {
        val circuitBreaker = circuitBreakerRegistry.circuitBreaker("cadastralRegistry")
        circuitBreaker.eventPublisher
            .onStateTransition { event ->
                log.warn("Cadastral circuit breaker state transition: {}", event.stateTransition)
            }
        return circuitBreaker
    }

    @Bean
    fun cadastralRetry(retryRegistry: RetryRegistry): Retry {
        val retry = retryRegistry.retry("cadastralRegistry")
        retry.eventPublisher
            .onRetry { event ->
                log.info(
                    "Retrying cadastral lookup (attempt {}): {}",
                    event.numberOfRetryAttempts,
                    event.lastThrowable?.message,
                )
            }
        return retry
    }
}
