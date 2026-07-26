package com.flagforge.domain.repository;

import com.flagforge.domain.evaluation.FlagSnapshot;
import com.flagforge.domain.flag.Environment;

import java.util.Optional;

/**
 * Port for fast flag lookup during evaluation (Redis or in-memory).
 */
public interface FlagCache {

	Optional<FlagSnapshot> get(String flagKey, Environment environment);

	void put(FlagSnapshot snapshot);

	void evict(String flagKey, Environment environment);

	void clear();

	/**
	 * Implementation name for diagnostics (e.g. "redis", "memory").
	 */
	String backend();
}
