package com.flagforge.infrastructure.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/** Helpers for the JWT {@link AuthenticatedUser} principal (not API-key principals). */
public final class SecurityUtils {

	private SecurityUtils() {
	}

	public static Optional<AuthenticatedUser> currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return Optional.empty();
		}
		Object principal = authentication.getPrincipal();
		if (principal instanceof AuthenticatedUser user) {
			return Optional.of(user);
		}
		return Optional.empty();
	}

	public static AuthenticatedUser requireCurrentUser() {
		return currentUser().orElseThrow(() ->
				new AccessDeniedException("Authentication required"));
	}

	public static UUID requireCurrentUserId() {
		return requireCurrentUser().id();
	}
}
