package com.flagforge.domain.evaluation;

import com.flagforge.domain.flag.Environment;
import com.flagforge.domain.flag.FeatureFlag;
import com.flagforge.domain.flag.FlagStatus;
import com.flagforge.domain.flag.FlagType;

import java.io.Serializable;
import java.util.UUID;

/**
 * Cache-friendly projection of a feature flag used by the evaluation engine.
 */
public record FlagSnapshot(
		UUID id,
		String key,
		Environment environment,
		boolean enabled,
		FlagStatus status,
		FlagType flagType,
		String defaultValue,
		Integer percentage,
		String rulesJson
) implements Serializable {

	public static FlagSnapshot from(FeatureFlag flag) {
		return new FlagSnapshot(
				flag.getId(),
				flag.getKey(),
				flag.getEnvironment(),
				flag.isEnabled(),
				flag.getStatus(),
				flag.getFlagType(),
				flag.getDefaultValue(),
				flag.getPercentage(),
				flag.getRulesJson()
		);
	}

	/** Composite key used by {@link com.flagforge.domain.repository.FlagCache}. */
	public String cacheKey() {
		return cacheKey(key, environment);
	}

	/** Format: {@code flagKey:ENVIRONMENT}. */
	public static String cacheKey(String key, Environment environment) {
		return key + ":" + environment.name();
	}
}
