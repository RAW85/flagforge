package com.flagforge.presentation.dto.flag;

import com.flagforge.domain.flag.FlagStatus;
import com.flagforge.domain.flag.FlagType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateFeatureFlagRequest(
		@Size(max = 255)
		String name,

		@Size(max = 2000)
		String description,

		FlagType flagType,

		FlagStatus status,

		@Size(max = 512)
		String defaultValue,

		@Min(0)
		@Max(100)
		Integer percentage,

		String rulesJson
) {
}
