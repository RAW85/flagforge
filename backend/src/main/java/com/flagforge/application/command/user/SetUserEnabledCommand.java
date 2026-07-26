package com.flagforge.application.command.user;

import java.util.UUID;

public record SetUserEnabledCommand(
		UUID userId,
		boolean enabled,
		UUID actorId
) {
}
