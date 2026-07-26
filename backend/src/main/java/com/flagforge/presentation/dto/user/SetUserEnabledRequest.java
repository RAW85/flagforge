package com.flagforge.presentation.dto.user;

import jakarta.validation.constraints.NotNull;

public record SetUserEnabledRequest(
		@NotNull
		Boolean enabled
) {
}
