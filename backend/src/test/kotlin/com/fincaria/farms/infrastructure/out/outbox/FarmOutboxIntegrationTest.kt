package com.fincaria.farms.infrastructure.out.outbox

import com.fincaria.PostgreSQLTestContainer
import com.fincaria.farms.application.port.`in`.CreateFarmCommand
import com.fincaria.farms.application.port.`in`.CreateFarmUseCase
import com.fincaria.farms.domain.CadastralReference
import com.fincaria.farms.domain.CropType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Verifies the outbox pattern's atomic write: creating a farm persists a
 * FarmRegistered event into the `outbox` table within the same transaction as
 * the farm itself. Runs the real wiring (use case -> FarmWriter -> recorder)
 * against Postgres; the end-to-end publish to Kafka is covered later in the
 * milestone with a Testcontainers broker.
 */
@SpringBootTest
// Transactional so the seeded rows roll back instead of polluting the shared
// DB; the service's own transaction joins this one, so the outbox row written
// by the recorder is visible to the assertions below.
@Transactional
class FarmOutboxIntegrationTest(
    @Autowired private val createFarm: CreateFarmUseCase,
    @Autowired private val jdbcTemplate: JdbcTemplate,
    @Autowired private val outboxJpaRepository: OutboxJpaRepository,
) : PostgreSQLTestContainer() {

    @Test
    fun `creating a farm writes a FarmRegistered event to the outbox`() {
        val userId = seedUser()

        val farm = createFarm(
            CreateFarmCommand(
                userId = userId.toString(),
                cadastralReference = CadastralReference(CADASTRAL_REFERENCE),
                cropTypes = setOf(CropType.OLIVE),
            ),
        )

        // JpaRepository.save() does not flush, so push the pending INSERT to the
        // database before reading the raw row back through JdbcTemplate.
        outboxJpaRepository.flush()

        val rows = jdbcTemplate.queryForList("SELECT * FROM outbox")
        assertThat(rows).hasSize(1)

        val row = rows.single()
        assertThat(row["aggregate_type"]).isEqualTo("Farm")
        assertThat(row["aggregate_id"]).isEqualTo(farm.id)
        assertThat(row["event_type"]).isEqualTo("FarmRegistered")
        assertThat(row["published_at"]).isNull()

        val payload = row["payload"].toString()
        // Stored as a real JSON object, not an escaped text blob (would start with a quote).
        assertThat(payload.trim()).startsWith("{")
        assertThat(payload).contains(farm.id, userId.toString(), CADASTRAL_REFERENCE, "OLIVE")
    }

    private fun seedUser(): UUID {
        val userId = UUID.randomUUID()
        jdbcTemplate.update(
            "INSERT INTO users (id, name, email, password_hash) VALUES (?, ?, ?, ?)",
            userId,
            "Test User",
            "user-$userId@example.com",
            "hash",
        )
        return userId
    }

    private companion object {
        // A valid reference distinct from the one FarmControllerTest commits to
        // the shared container, so this test never collides on the uniqueness
        // constraint regardless of test ordering.
        const val CADASTRAL_REFERENCE = "7837301VG8173B0002YY"
    }
}
