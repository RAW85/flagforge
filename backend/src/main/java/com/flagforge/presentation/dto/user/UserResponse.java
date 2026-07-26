package com.flagforge.presentation.dto.user;

import com.flagforge.domain.user.User;
import com.flagforge.domain.user.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
		UUID id,
		String username,
		String email,
		UserRole role,
		boolean enabled,
		Instant createdAt,
		Instant updatedAt
) {

	public static UserResponse from(User user) {
		return new UserResponse(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getRole(),
				user.isEnabled(),
				user.getCreatedAt(),
				user.getUpdatedAt()
		);
	}
}
