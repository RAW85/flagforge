package com.flagforge.application.command.flag;

import java.util.UUID;

public record DeleteFeatureFlagCommand(
		UUID id,
		UUID actorId
) {
}
