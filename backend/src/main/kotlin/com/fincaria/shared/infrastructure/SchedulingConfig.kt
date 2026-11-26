package com.fincaria.shared.infrastructure

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Enables Spring's scheduled task execution (`@Scheduled`), used by the outbox
 * relay to poll for and publish pending events.
 */
@Configuration
@EnableScheduling
class SchedulingConfig
