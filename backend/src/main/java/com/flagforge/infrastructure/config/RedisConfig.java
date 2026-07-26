package com.flagforge.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@ConditionalOnProperty(name = "flagforge.cache.type", havingValue = "redis")
public class RedisConfig {

	@Bean
	StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}

	/**
	 * Dedicated mapper for cache payloads when a Spring ObjectMapper bean is absent.
	 */
	@Bean
	ObjectMapper flagCacheObjectMapper() {
		return new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}
}
