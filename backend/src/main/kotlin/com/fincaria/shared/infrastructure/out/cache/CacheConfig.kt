package com.fincaria.shared.infrastructure.out.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import java.time.Duration

/**
 * Backs Spring Cache with Redis. Cached values are stored as JSON with type
 * hints (so Kotlin data classes round-trip correctly) and expire after a fixed
 * TTL — the crop type catalog is a controlled, rarely-changing vocabulary, so
 * a time-based window is enough and no explicit eviction is wired.
 */
@Configuration
@EnableCaching
class CacheConfig {
    @Bean
    fun redisCacheConfiguration(): RedisCacheConfiguration {
        // Restrict polymorphic typing to our own types and JDK collections to
        // avoid the well-known deserialization gadget risk of open default typing.
        val typeValidator = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType("com.fincaria.")
            .allowIfSubType("java.util.")
            .build()
        val objectMapper = ObjectMapper()
            .registerKotlinModule()
            .activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL)
        val serializer = GenericJackson2JsonRedisSerializer(objectMapper)

        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(24))
            .disableCachingNullValues()
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
    }
}
