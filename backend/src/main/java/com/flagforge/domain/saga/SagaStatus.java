package com.flagforge.domain.saga;

/** Lifecycle of a progressive rollout saga. */
public enum SagaStatus {
	/** Actively stepping through percentages. */
	RUNNING,
	/** Reached final step (typically 100%). */
	COMPLETED,
	/** Compensated: flag disabled, percentage reset to first step. */
	ROLLED_BACK,
	/** Reserved for automatic failure handling. */
	FAILED
}
