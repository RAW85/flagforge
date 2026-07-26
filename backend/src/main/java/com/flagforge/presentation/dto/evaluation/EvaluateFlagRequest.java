package com.flagforge.presentation.dto.evaluation;

import com.flagforge.domain.flag.Environment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body for POST evaluate (dashboard and SDK).
 *
 * @param subjectId sticky bucketing key (user id, account id, etc.)
 */
public record EvaluateFlagRequest(
		@NotBlank
		@Size(max = 128)
		String flagKey,

		@NotNull
		Environment environment,

		@NotBlank
		@Size(max = 255)
		String subjectId,

		/**
		 * Optional targeting context as JSON string.
		 */
		String contextJson,

		/**
		 * When true, persist an evaluation history row (default false for hot path).
		 */
		Boolean record
) {
}
