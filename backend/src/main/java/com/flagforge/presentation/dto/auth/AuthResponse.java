package com.flagforge.presentation.dto.auth;

import com.flagforge.application.command.auth.AuthResult;
import com.flagforge.domain.user.UserRole;

import java.util.UUID;

public record AuthResponse(
		UUID userId,
		String username,
		String email,
		UserRole role,
		String accessToken,
		String tokenType,
		long expiresInMs
) {

	public static AuthResponse from(AuthResult result) {
		return new AuthResponse(
				result.userId(),
				result.username(),
				result.email(),
				result.role(),
				result.accessToken(),
				result.tokenType(),
				result.expiresInMs()
		);
	}
}
