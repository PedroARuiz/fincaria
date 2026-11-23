package com.fincaria.farms.infrastructure.out.persistence

import com.fincaria.PostgreSQLTestContainer
import jakarta.persistence.EntityManagerFactory
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.SessionFactory
import org.hibernate.stat.Statistics
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Guards against the crop-types N+1 at the persistence boundary: listing a page
 * of farms must not issue one query per farm to load its crop types. Exercised
 * directly against the JPA repository (not the domain) so arbitrary cadastral
 * references can be seeded without the value object's checksum validation.
 */
@SpringBootTest
@TestPropertySource(properties = ["spring.jpa.properties.hibernate.generate_statistics=true"])
// Transactional so the session stays open while the lazy collections are
// initialized, and so the seeded rows roll back instead of polluting the shared DB.
@Transactional
class FarmListingQueryCountTest(
    @Autowired private val farmJpaRepository: FarmJpaRepository,
    @Autowired private val jdbcTemplate: JdbcTemplate,
    @Autowired entityManagerFactory: EntityManagerFactory,
) : PostgreSQLTestContainer() {
    private val statistics: Statistics =
        entityManagerFactory.unwrap(SessionFactory::class.java).statistics

    @Test
    fun `listing farms issues the same number of queries regardless of how many farms there are`() {
        val fewFarmsUser = seedUser(farmCount = 2)
        val manyFarmsUser = seedUser(farmCount = 6)

        val statementsForFew = countStatements(fewFarmsUser)
        val statementsForMany = countStatements(manyFarmsUser)

        // Measured before/after: without batching (lazy collection, the N+1)
        // it is 1 + N statements — 2 farms = 3, 6 farms = 7 — so the two users
        // differ. With @BatchSize the collection loads collapse into one extra
        // query — both = 2 (1 root + 1 batched load) — so the count is constant
        // and does not grow with the farm count.
        assertThat(statementsForFew).isEqualTo(statementsForMany)
    }

    private fun countStatements(userId: UUID): Long {
        statistics.clear()
        val page = farmJpaRepository.findByUserId(userId, PageRequest.of(0, 20))
        // Touch each collection to trigger its (batched) initialization.
        page.content.forEach { it.cropTypes.size }
        return statistics.prepareStatementCount
    }

    private fun seedUser(farmCount: Int): UUID {
        val userId = UUID.randomUUID()
        jdbcTemplate.update(
            "INSERT INTO users (id, name, email, password_hash) VALUES (?, ?, ?, ?)",
            userId,
            "Test User",
            "user-$userId@example.com",
            "hash",
        )
        repeat(farmCount) {
            val farmId = UUID.randomUUID()
            val reference = UUID.randomUUID().toString().replace("-", "").substring(0, 20)
            jdbcTemplate.update(
                "INSERT INTO farms (id, user_id, cadastral_reference, verification_status) " +
                    "VALUES (?, ?, ?, 'UNVERIFIED')",
                farmId,
                userId,
                reference,
            )
            jdbcTemplate.update(
                "INSERT INTO farm_crop_types (farm_id, crop_type) VALUES (?, 'OLIVE')",
                farmId,
            )
        }
        return userId
    }
}
