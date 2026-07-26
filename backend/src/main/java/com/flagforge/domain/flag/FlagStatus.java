package com.flagforge.domain.flag;

/**
 * Lifecycle status of a feature flag.
 * Only {@link #ACTIVE} flags participate in evaluation when also enabled.
 */
public enum FlagStatus {
	/** Created but not yet live; enabling promotes to ACTIVE. */
	DRAFT,
	/** Eligible for evaluation when {@code enabled=true}. */
	ACTIVE,
	/** Soft-retired; cannot be re-enabled. */
	ARCHIVED
}
