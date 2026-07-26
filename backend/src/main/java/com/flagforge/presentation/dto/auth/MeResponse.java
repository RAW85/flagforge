package com.flagforge.presentation.dto.auth;

import com.flagforge.domain.user.UserRole;
import com.flagforge.infrastructure.security.AuthenticatedUser;

import java.util.UUID;

public record MeResponse(
		UUID id,
		String username,
		String email,
		UserRole role
) {

	public static MeResponse from(AuthenticatedUser user) {
		return new MeResponse(user.id(), user.username(), user.email(), user.role());
	}
}
