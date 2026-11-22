package com.fincaria.farms.infrastructure.out.persistence

import com.fincaria.PostgreSQLTestContainer
import com.fincaria.farms.application.port.`in`.GetCropTypeCatalogUseCase
import com.fincaria.farms.domain.CropType
import com.fincaria.farms.domain.CropTypeCatalogEntry
import com.fincaria.farms.infrastructure.out.persistence.CropTypeCatalogRepositoryImpl.Companion.CROP_TYPE_CATALOG_CACHE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.cache.interceptor.SimpleKey

@SpringBootTest
class CropTypeCatalogCacheTest(
    @Autowired private val getCropTypeCatalogUseCase: GetCropTypeCatalogUseCase,
    @Autowired private val cacheManager: CacheManager,
) : PostgreSQLTestContainer() {
    @Test
    fun `the catalog read is served from the Redis cache, bypassing the database`() {
        val cache = requireNotNull(cacheManager.getCache(CROP_TYPE_CATALOG_CACHE))
        // Pre-seed the cache with a value the database would never return. If the
        // read is served from the cache (not the DB), the use case returns this.
        // This also exercises the real Redis JSON serialize/deserialize round-trip.
        val cachedOnly = listOf(CropTypeCatalogEntry(CropType.OLIVE, "Cached-only label", null, 99))
        cache.put(SimpleKey.EMPTY, cachedOnly)
        try {
            assertThat(getCropTypeCatalogUseCase()).isEqualTo(cachedOnly)
        } finally {
            cache.evict(SimpleKey.EMPTY)
        }
    }
}
