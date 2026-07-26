package com.flagforge.domain.evaluation;

import com.flagforge.domain.flag.Environment;
import com.flagforge.domain.flag.FlagType;

import java.util.UUID;

/**
 * Outcome of evaluating a feature flag for a subject.
 *
 * @param value   string form of the resolved result (boolean, variant name, or default)
 * @param enabled true when the subject is in the feature (on / in-rollout / variant assigned)
 * @param bucket  sticky 0–99 bucket used for percentage and multivariate weighting
 */
public record EvaluationResult(
		UUID flagId,
		String flagKey,
		Environment environment,
		FlagType flagType,
		String subjectId,
		String value,
		boolean enabled,
		EvaluationReason reason,
		Integer bucket
) {
}
