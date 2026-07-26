package com.flagforge.presentation.dto.flag;

import com.flagforge.domain.flag.Environment;
import com.flagforge.domain.flag.FlagType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateFeatureFlagRequest(
		@NotBlank
		@Size(max = 128)
		@Pattern(
				regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
				message = "key must be kebab-case (lowercase letters, digits, hyphens)"
		)
		String key,

		@NotBlank
		@Size(max = 255)
		String name,

		@Size(max = 2000)
		String description,

		@NotNull
		FlagType flagType,

		@NotNull
		Environment environment,

		@Size(max = 512)
		String defaultValue,

		@Min(0)
		@Max(100)
		Integer percentage,

		String rulesJson
) {
}
