package com.flagforge.presentation.dto.user;

import com.flagforge.domain.user.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
		@NotNull
		UserRole role
) {
}
