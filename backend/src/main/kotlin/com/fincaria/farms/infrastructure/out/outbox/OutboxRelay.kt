package com.fincaria.farms.infrastructure.out.outbox

import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.Limit
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

/**
 * Polling half of the outbox pattern: on a fixed schedule it reads a batch of
 * unpublished events, publishes each to Kafka and marks the ones that made it.
 *
 * Publishing happens *before* marking, so a crash in between leaves the event
 * pending and it is re-published next tick — the at-least-once guarantee that
 * makes consumers need to be idempotent. The message key is the aggregate id,
 * so all events of one farm land on the same partition and stay ordered, while
 * different farms spread across partitions and process in parallel.
 */
@Component
@ConditionalOnProperty(name = ["fincaria.outbox.relay.enabled"], havingValue = "true", matchIfMissing = true)
class OutboxRelay(
    private val outboxJpaRepository: OutboxJpaRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @Value("\${fincaria.outbox.relay.batch-size}") private val batchSize: Int,
    @Value("\${fincaria.outbox.relay.topic}") private val topic: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "\${fincaria.outbox.relay.poll-delay-ms}")
    fun publishPendingEvents() {
        val pending = outboxJpaRepository.findByPublishedAtIsNullOrderByOccurredAtAsc(Limit.of(batchSize))
        if (pending.isEmpty()) {
            return
        }

        val publishedIds = ArrayList<UUID>(pending.size)
        for (event in pending) {
            try {
                publish(event)
            } catch (ex: Exception) {
                // Stop at the first failure so that events of the same aggregate
                // keep their order: the next tick resumes from this event. The
                // already-published ones below are still marked.
                log.warn("Outbox relay stopped at event {} ({}): {}", event.id, event.eventType, ex.message)
                break
            }
            publishedIds.add(event.id)
        }

        if (publishedIds.isNotEmpty()) {
            outboxJpaRepository.markPublished(publishedIds, Instant.now())
            log.debug("Outbox relay published {} event(s)", publishedIds.size)
        }
    }

    private fun publish(event: OutboxJpaEntity) {
        val record = ProducerRecord(topic, event.aggregateId, event.payload)
        record.headers()
            .add(HEADER_EVENT_ID, event.id.toString().toByteArray())
            .add(HEADER_EVENT_TYPE, event.eventType.toByteArray())
            .add(HEADER_AGGREGATE_TYPE, event.aggregateType.toByteArray())
        // Block on the broker acknowledgement (acks=all) so only events Kafka
        // has actually accepted get marked as published.
        kafkaTemplate.send(record).get()
    }

    private companion object {
        const val HEADER_EVENT_ID = "event-id"
        const val HEADER_EVENT_TYPE = "event-type"
        const val HEADER_AGGREGATE_TYPE = "aggregate-type"
    }
}
