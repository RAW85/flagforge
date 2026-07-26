package com.flagforge.presentation.dto.saga;

import jakarta.validation.constraints.Size;

public record RollbackRolloutSagaRequest(
		@Size(max = 1000)
		String reason
) {
}
