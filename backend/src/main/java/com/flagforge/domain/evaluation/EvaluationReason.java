package com.flagforge.domain.evaluation;

/**
 * Why a particular evaluation result was chosen.
 */
public enum EvaluationReason {
	/** Flag is disabled, draft, or archived — default value returned. */
	FLAG_DISABLED,
	/** Boolean flag is on. */
	BOOLEAN_ENABLED,
	/** Subject fell inside the percentage rollout bucket. */
	PERCENTAGE_IN,
	/** Subject fell outside the percentage rollout bucket. */
	PERCENTAGE_OUT,
	/** Multivariate variant selected by sticky weight. */
	VARIANT_MATCH,
	/** Fallback when configuration is incomplete. */
	DEFAULT_VALUE
}
