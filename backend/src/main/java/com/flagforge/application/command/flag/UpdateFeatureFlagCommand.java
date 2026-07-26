package com.flagforge.application.command.flag;

import com.flagforge.domain.flag.FlagStatus;
import com.flagforge.domain.flag.FlagType;

import java.util.UUID;

public record UpdateFeatureFlagCommand(
		UUID id,
		String name,
		String description,
		FlagType flagType,
		FlagStatus status,
		String defaultValue,
		Integer percentage,
		String rulesJson,
		UUID actorId
) {
}
