package com.fincaria.farms.infrastructure.out.persistence

import com.fincaria.PostgreSQLTestContainer
import com.fincaria.farms.application.port.`in`.GetCropTypeCatalogUseCase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
// Transactional so the catalog change made behind the cache's back rolls back
// instead of polluting the shared database.
@Transactional
class CropTypeCatalogCacheTest(
    @Autowired private val getCropTypeCatalogUseCase: GetCropTypeCatalogUseCase,
    @Autowired private val jdbcTemplate: JdbcTemplate,
) : PostgreSQLTestContainer() {
    @Test
    fun `the catalog is served from the cache, not re-read from the database`() {
        // First read: populates the cache (or hits an already-warm one).
        val first = getCropTypeCatalogUseCase()
        assertThat(first).isNotEmpty()

        // Change the catalog in the database behind the cache's back.
        jdbcTemplate.update("UPDATE crop_types SET label = 'Changed in DB' WHERE code = 'OLIVE'")

        // The second read still returns the cached value — the DB change is
        // invisible — proving it was served from Redis, not re-queried from
        // Postgres. Robust to whatever the cache already held: both reads see the
        // same cached snapshot, which the DB change never reaches.
        val second = getCropTypeCatalogUseCase()
        assertThat(second).isEqualTo(first)
        assertThat(second.none { it.label == "Changed in DB" }).isTrue()
    }
}
