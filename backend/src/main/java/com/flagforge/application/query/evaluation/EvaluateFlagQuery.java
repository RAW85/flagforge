package com.flagforge.application.query.evaluation;

import com.flagforge.domain.flag.Environment;

/**
 * Evaluate {@code flagKey} in {@code environment} for {@code subjectId}.
 *
 * @param contextJson optional targeting attributes (JSON); stored only when recording
 * @param record      when true, write an evaluation history row (off by default on hot path)
 */
public record EvaluateFlagQuery(
		String flagKey,
		Environment environment,
		String subjectId,
		String contextJson,
		boolean record
) {
}
