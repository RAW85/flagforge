package com.flagforge.presentation.dto.saga;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record StartRolloutSagaRequest(
		@NotNull
		UUID flagId,

		/**
		 * Optional progressive percentages. Defaults to 0,10,25,50,100.
		 */
		List<Integer> steps
) {
}
