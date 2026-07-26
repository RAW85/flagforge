package com.flagforge.presentation.dto.apikey;

import com.flagforge.domain.flag.Environment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateApiKeyRequest(
		@NotBlank
		@Size(max = 100)
		String name,

		/**
		 * Optional. When set, the key can only evaluate flags in this environment.
		 */
		Environment environmentScope
) {
}
