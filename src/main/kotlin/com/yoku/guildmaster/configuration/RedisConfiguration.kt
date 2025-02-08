package com.yoku.guildmaster.configuration

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.interceptor.KeyGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration


@Configuration
@EnableCaching
class RedisConfiguration(
    private val objectMapper: ObjectMapper  // Inject the configured ObjectMapper
) {
    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        return LettuceConnectionFactory()
    }

    @Bean
    fun cacheManager(): CacheManager {
        val mapper = configureObjectMapper()

        val cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(6))
            .prefixCacheNameWith("guildmaster::")
            .disableCachingNullValues()
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer(mapper)  // Use the configured ObjectMapper
                ))

        return RedisCacheManager.builder(redisConnectionFactory())
            .cacheDefaults(cacheConfig)
            .build()
    }

    @Bean
    fun <T> redisTemplate(): RedisTemplate<String, T> {
        val template = RedisTemplate<String, T>()
        template.connectionFactory = redisConnectionFactory()

        val mapper = configureObjectMapper()

        val jsonSerializer = GenericJackson2JsonRedisSerializer(mapper)
        template.valueSerializer = jsonSerializer
        template.hashValueSerializer = jsonSerializer
        template.keySerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()

        return template
    }

    private fun configureObjectMapper(): ObjectMapper {
        val mapper = objectMapper.copy()
            .activateDefaultTyping(
                objectMapper.polymorphicTypeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
            )
        return mapper
    }

    @Bean
    fun keyGenerator(): KeyGenerator {
        return KeyGenerator { _, method, params ->
            "${method.name}_${params.joinToString("_")}"
        }
    }

}