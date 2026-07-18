package com.AIPoweredOrder.LogisticsPlatform.user_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisConfig implements CachingConfigurer {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    @Bean
    public CacheManager cacheManager() {
        // Profiles change infrequently; the service also evicts explicitly on every
        // update, so a longer safety-net TTL is fine here.
        Map<String, RedisCacheConfiguration> perCacheConfig = Map.of(
                CacheNames.USERS, baseConfig().entryTtl(Duration.ofMinutes(15))
        );

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(baseConfig().entryTtl(Duration.ofMinutes(10)))
                .withInitialCacheConfigurations(perCacheConfig)
                .build();
    }

    /**
     * Redis is shared with other services on the same host/port; namespace every key with
     * the owning service so cache names can never collide across services.
     */
    private RedisCacheConfiguration baseConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> "user-service::" + cacheName + "::")
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()));
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new RedisCacheErrorHandler();
    }
}
