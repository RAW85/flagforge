package com.flagforge.infrastructure.cache;

import com.flagforge.domain.evaluation.FlagSnapshot;
import com.flagforge.domain.flag.Environment;
import com.flagforge.domain.repository.FlagCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default cache when Redis is not configured. Process-local only.
 */
@Component
@ConditionalOnProperty(name = "flagforge.cache.type", havingValue = "memory", matchIfMissing = true)
public class InMemoryFlagCache implements FlagCache {

	private final ConcurrentHashMap<String, FlagSnapshot> store = new ConcurrentHashMap<>();

	@Override
	public Optional<FlagSnapshot> get(String flagKey, Environment environment) {
		return Optional.ofNullable(store.get(FlagSnapshot.cacheKey(flagKey, environment)));
	}

	@Override
	public void put(FlagSnapshot snapshot) {
		store.put(snapshot.cacheKey(), snapshot);
	}

	@Override
	public void evict(String flagKey, Environment environment) {
		store.remove(FlagSnapshot.cacheKey(flagKey, environment));
	}

	@Override
	public void clear() {
		store.clear();
	}

	@Override
	public String backend() {
		return "memory";
	}
}
