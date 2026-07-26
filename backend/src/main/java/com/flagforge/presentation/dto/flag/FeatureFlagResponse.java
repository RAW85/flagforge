package com.flagforge.presentation.dto.flag;

import com.flagforge.domain.flag.Environment;
import com.flagforge.domain.flag.FeatureFlag;
import com.flagforge.domain.flag.FlagStatus;
import com.flagforge.domain.flag.FlagType;

import java.time.Instant;
import java.util.UUID;

public record FeatureFlagResponse(
		UUID id,
		String key,
		String name,
		String description,
		boolean enabled,
		FlagStatus status,
		FlagType flagType,
		Environment environment,
		String defaultValue,
		Integer percentage,
		String rulesJson,
		UUID createdBy,
		Instant createdAt,
		Instant updatedAt,
		Long version
) {

	public static FeatureFlagResponse from(FeatureFlag flag) {
		return new FeatureFlagResponse(
				flag.getId(),
				flag.getKey(),
				flag.getName(),
				flag.getDescription(),
				flag.isEnabled(),
				flag.getStatus(),
				flag.getFlagType(),
				flag.getEnvironment(),
				flag.getDefaultValue(),
				flag.getPercentage(),
				flag.getRulesJson(),
				flag.getCreatedBy(),
				flag.getCreatedAt(),
				flag.getUpdatedAt(),
				flag.getVersion()
		);
	}
}
