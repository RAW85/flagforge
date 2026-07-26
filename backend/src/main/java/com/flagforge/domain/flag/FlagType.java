package com.flagforge.domain.flag;

/**
 * Evaluation strategy for a feature flag.
 */
public enum FlagType {
	/** On/off for all subjects when enabled + ACTIVE. */
	BOOLEAN,
	/** Sticky percentage rollout via CRC32 bucket 0–99. */
	PERCENTAGE,
	/** Weighted variants from {@code rulesJson}. */
	MULTIVARIATE
}
