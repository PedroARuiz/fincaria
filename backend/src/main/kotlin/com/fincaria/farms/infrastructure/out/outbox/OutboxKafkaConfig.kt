package com.fincaria.farms.infrastructure.out.outbox

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

/**
 * Provisions the Kafka topic the outbox relay publishes to. Declaring it as a
 * [NewTopic] bean has KafkaAdmin create it explicitly at startup, rather than
 * relying on broker-side auto-creation (which is disabled), so a wrong topic
 * name fails loudly instead of silently creating a stray topic.
 *
 * Gated by the same flag as the relay so that context tests without a broker
 * neither schedule the relay nor attempt topic provisioning.
 */
@Configuration
@ConditionalOnProperty(name = ["fincaria.outbox.relay.enabled"], havingValue = "true", matchIfMissing = true)
class OutboxKafkaConfig(
    @Value("\${fincaria.outbox.relay.topic}") private val topic: String,
    @Value("\${fincaria.outbox.relay.topic-partitions}") private val partitions: Int,
    @Value("\${fincaria.outbox.relay.topic-replicas}") private val replicas: Int,
) {
    @Bean
    fun farmEventsTopic(): NewTopic =
        TopicBuilder.name(topic)
            .partitions(partitions)
            .replicas(replicas)
            .build()
}
