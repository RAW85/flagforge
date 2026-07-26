package com.flagforge.presentation.dto.flag;

import jakarta.validation.constraints.NotNull;

public record ToggleFeatureFlagRequest(
		@NotNull
		Boolean enabled
) {
}
