package com.flagforge.domain.user;

/**
 * Dashboard RBAC roles (mapped to {@code ROLE_*} authorities).
 */
public enum UserRole {
	/** Full access including user admin and flag delete. */
	ADMIN,
	/** Create/update flags, API keys, and rollouts. */
	EDITOR,
	/** Read flags/rollouts and evaluate via JWT. */
	VIEWER
}
