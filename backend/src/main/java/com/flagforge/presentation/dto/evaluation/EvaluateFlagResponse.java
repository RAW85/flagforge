package com.flagforge.presentation.dto.evaluation;

import com.flagforge.domain.evaluation.EvaluationReason;
import com.flagforge.domain.evaluation.EvaluationResult;
import com.flagforge.domain.flag.Environment;
import com.flagforge.domain.flag.FlagType;

import java.util.UUID;

/**
 * Evaluation API response.
 *
 * @param value   resolved payload for clients ({@code "true"}/{@code "false"}, variant name, etc.)
 * @param enabled whether the subject is treated as in the feature (use this for on/off gates)
 * @param reason  why this result was chosen
 * @param bucket  sticky 0–99 bucket for the subject (null only if engine omits it)
 */
public record EvaluateFlagResponse(
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

	public static EvaluateFlagResponse from(EvaluationResult result) {
		return new EvaluateFlagResponse(
				result.flagId(),
				result.flagKey(),
				result.environment(),
				result.flagType(),
				result.subjectId(),
				result.value(),
				result.enabled(),
				result.reason(),
				result.bucket()
		);
	}
}
