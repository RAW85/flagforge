package com.flagforge.application.command.auth;

import com.flagforge.domain.user.User;
import com.flagforge.domain.user.UserRole;

import java.util.UUID;

public record AuthResult(
		UUID userId,
		String username,
		String email,
		UserRole role,
		String accessToken,
		String tokenType,
		long expiresInMs
) {

	public static AuthResult of(User user, String token, long expiresInMs) {
		return new AuthResult(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getRole(),
				token,
				"Bearer",
				expiresInMs
		);
	}
}
