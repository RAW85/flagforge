package com.flagforge.presentation.rest;

import com.flagforge.application.command.user.SetUserEnabledCommand;
import com.flagforge.application.command.user.UpdateUserRoleCommand;
import com.flagforge.application.command.user.UserCommandHandler;
import com.flagforge.application.query.user.ListUsersQuery;
import com.flagforge.application.query.user.UserQueryHandler;
import com.flagforge.domain.user.User;
import com.flagforge.infrastructure.security.SecurityUtils;
import com.flagforge.presentation.dto.user.SetUserEnabledRequest;
import com.flagforge.presentation.dto.user.UpdateUserRoleRequest;
import com.flagforge.presentation.dto.user.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** ADMIN-only user list, role changes, and enable/disable. */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Admin user and role management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

	private final UserQueryHandler queryHandler;
	private final UserCommandHandler commandHandler;

	@GetMapping
	@Operation(summary = "List all users (ADMIN only)")
	public List<UserResponse> list() {
		return queryHandler.handle(new ListUsersQuery()).stream()
				.map(UserResponse::from)
				.toList();
	}

	@PutMapping("/{id}/role")
	@Operation(summary = "Update a user's role (ADMIN only)")
	public UserResponse updateRole(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateUserRoleRequest request
	) {
		UUID actorId = SecurityUtils.requireCurrentUserId();
		User updated = commandHandler.handle(new UpdateUserRoleCommand(id, request.role(), actorId));
		return UserResponse.from(updated);
	}

	@PutMapping("/{id}/enabled")
	@Operation(summary = "Enable or disable a user (ADMIN only)")
	public UserResponse setEnabled(
			@PathVariable UUID id,
			@Valid @RequestBody SetUserEnabledRequest request
	) {
		UUID actorId = SecurityUtils.requireCurrentUserId();
		User updated = commandHandler.handle(
				new SetUserEnabledCommand(id, request.enabled(), actorId)
		);
		return UserResponse.from(updated);
	}
}
