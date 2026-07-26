package com.flagforge.infrastructure.cache;

import com.flagforge.domain.evaluation.FlagSnapshot;
import com.flagforge.domain.flag.Environment;
import com.flagforge.domain.repository.FlagCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis-backed flag snapshot cache for low-latency evaluation.
 */
@Component
@ConditionalOnProperty(name = "flagforge.cache.type", havingValue = "redis")
@Slf4j
public class RedisFlagCache implements FlagCache {

	private static final String KEY_PREFIX = "flagforge:flag:";
	private static final Duration TTL = Duration.ofMinutes(30);

	private final StringRedisTemplate redis;
	private final ObjectMapper objectMapper;

	public RedisFlagCache(StringRedisTemplate redis, ObjectMapper objectMapper) {
		this.redis = redis;
		this.objectMapper = objectMapper;
	}

	@Override
	public Optional<FlagSnapshot> get(String flagKey, Environment environment) {
		String raw = redis.opsForValue().get(redisKey(flagKey, environment));
		if (raw == null || raw.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(objectMapper.readValue(raw, FlagSnapshot.class));
		} catch (JsonProcessingException ex) {
			log.warn("Failed to deserialize flag cache entry for {}:{}", flagKey, environment, ex);
			evict(flagKey, environment);
			return Optional.empty();
		}
	}

	@Override
	public void put(FlagSnapshot snapshot) {
		try {
			String json = objectMapper.writeValueAsString(snapshot);
			redis.opsForValue().set(redisKey(snapshot.key(), snapshot.environment()), json, TTL);
		} catch (JsonProcessingException ex) {
			log.warn("Failed to serialize flag cache entry for {}", snapshot.cacheKey(), ex);
		}
	}

	@Override
	public void evict(String flagKey, Environment environment) {
		redis.delete(redisKey(flagKey, environment));
	}

	@Override
	public void clear() {
		var keys = redis.keys(KEY_PREFIX + "*");
		if (keys != null && !keys.isEmpty()) {
			redis.delete(keys);
		}
	}

	@Override
	public String backend() {
		return "redis";
	}

	private String redisKey(String flagKey, Environment environment) {
		return KEY_PREFIX + FlagSnapshot.cacheKey(flagKey, environment);
	}
}
