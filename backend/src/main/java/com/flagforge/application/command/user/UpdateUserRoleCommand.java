package com.flagforge.application.command.user;

import com.flagforge.domain.user.UserRole;

import java.util.UUID;

public record UpdateUserRoleCommand(
		UUID userId,
		UserRole role,
		UUID actorId
) {
}
