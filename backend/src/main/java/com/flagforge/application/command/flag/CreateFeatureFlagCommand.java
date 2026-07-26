package com.flagforge.application.command.flag;

import com.flagforge.domain.flag.Environment;
import com.flagforge.domain.flag.FlagType;

import java.util.UUID;

public record CreateFeatureFlagCommand(
		String key,
		String name,
		String description,
		FlagType flagType,
		Environment environment,
		String defaultValue,
		Integer percentage,
		String rulesJson,
		UUID createdBy
) {
}
