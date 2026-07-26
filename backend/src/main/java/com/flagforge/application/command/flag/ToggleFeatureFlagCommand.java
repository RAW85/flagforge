package com.flagforge.application.command.flag;

import java.util.UUID;

public record ToggleFeatureFlagCommand(
		UUID id,
		boolean enabled,
		UUID actorId
) {
}
