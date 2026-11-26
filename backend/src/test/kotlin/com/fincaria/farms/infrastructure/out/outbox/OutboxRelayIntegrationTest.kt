package com.fincaria.farms.infrastructure.out.outbox

import com.fincaria.PostgreSQLTestContainer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.jdbc.core.JdbcTemplate
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName
import tools.jackson.databind.ObjectMapper
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant
import java.util.Properties
import java.util.UUID

/**
 * Verifies the relay end of the outbox pattern against a real Kafka broker:
 * a pending outbox row is published to the topic (value + headers) and then
 * marked as published. The relay is enabled only for this test (inline
 * property); a short poll delay keeps the test fast.
 */
@SpringBootTest(
    properties = [
        "fincaria.outbox.relay.enabled=true",
        "fincaria.outbox.relay.poll-delay-ms=200",
    ],
)
class OutboxRelayIntegrationTest(
    @Autowired private val jdbcTemplate: JdbcTemplate,
    @Autowired private val objectMapper: ObjectMapper,
) : PostgreSQLTestContainer() {

    @Test
    fun `the relay publishes a pending event to kafka and marks it published`() {
        val eventId = UUID.randomUUID()
        val aggregateId = UUID.randomUUID().toString()
        val payload = """{"farmId":"$aggregateId","cropTypes":["OLIVE"]}"""
        insertPendingEvent(eventId, aggregateId, payload)

        newConsumer().use { consumer ->
            consumer.subscribe(listOf(TOPIC))
            val record = awaitRecordWithKey(consumer, aggregateId)

            // Compare as JSON, not byte-for-byte: the payload round-trips
            // through a jsonb column, which reformats it (whitespace, key order).
            assertThat(objectMapper.readTree(record.value())).isEqualTo(objectMapper.readTree(payload))
            assertThat(headerValue(record, "event-id")).isEqualTo(eventId.toString())
            assertThat(headerValue(record, "event-type")).isEqualTo("FarmRegistered")
            assertThat(headerValue(record, "aggregate-type")).isEqualTo("Farm")
        }

        await().atMost(Duration.ofSeconds(10)).untilAsserted {
            val marked = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox WHERE id = ? AND published_at IS NOT NULL",
                Int::class.java,
                eventId,
            )
            assertThat(marked).isEqualTo(1)
        }

        jdbcTemplate.update("DELETE FROM outbox WHERE id = ?", eventId)
    }

    private fun insertPendingEvent(eventId: UUID, aggregateId: String, payload: String) {
        jdbcTemplate.update(
            "INSERT INTO outbox (id, aggregate_type, aggregate_id, event_type, payload, occurred_at) " +
                "VALUES (?, 'Farm', ?, 'FarmRegistered', ?::jsonb, ?)",
            eventId,
            aggregateId,
            payload,
            Timestamp.from(Instant.now()),
        )
    }

    private fun newConsumer(): KafkaConsumer<String, String> {
        val props = Properties()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafka.bootstrapServers
        props[ConsumerConfig.GROUP_ID_CONFIG] = "test-${UUID.randomUUID()}"
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        return KafkaConsumer(props)
    }

    private fun awaitRecordWithKey(
        consumer: KafkaConsumer<String, String>,
        key: String,
    ): ConsumerRecord<String, String> {
        val deadline = System.currentTimeMillis() + 15_000
        while (System.currentTimeMillis() < deadline) {
            val match = consumer.poll(Duration.ofMillis(500)).firstOrNull { it.key() == key }
            if (match != null) {
                return match
            }
        }
        throw AssertionError("No record with key $key received within the timeout")
    }

    private fun headerValue(record: ConsumerRecord<String, String>, name: String): String =
        String(record.headers().lastHeader(name).value())

    private companion object {
        const val TOPIC = "farm-events"

        @JvmStatic
        @ServiceConnection
        val kafka: KafkaContainer = KafkaContainer(DockerImageName.parse("apache/kafka:4.4.0"))

        init {
            kafka.start()
        }
    }
}
