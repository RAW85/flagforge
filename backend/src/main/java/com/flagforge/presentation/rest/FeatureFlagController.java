package com.flagforge.presentation.rest;

import com.flagforge.application.command.flag.CreateFeatureFlagCommand;
import com.flagforge.application.command.flag.DeleteFeatureFlagCommand;
import com.flagforge.application.command.flag.FeatureFlagCommandHandler;
import com.flagforge.application.command.flag.ToggleFeatureFlagCommand;
import com.flagforge.application.command.flag.UpdateFeatureFlagCommand;
import com.flagforge.application.query.flag.FeatureFlagQueryHandler;
import com.flagforge.application.query.flag.GetFeatureFlagByKeyQuery;
import com.flagforge.application.query.flag.GetFeatureFlagQuery;
import com.flagforge.application.query.flag.ListFeatureFlagsQuery;
import com.flagforge.domain.common.CursorPage;
import com.flagforge.domain.common.CursorPageRequest;
import com.flagforge.domain.flag.Environment;
import com.flagforge.domain.flag.FeatureFlag;
import com.flagforge.domain.flag.FlagStatus;
import com.flagforge.infrastructure.security.SecurityUtils;
import com.flagforge.presentation.dto.common.CursorPageResponse;
import com.flagforge.presentation.dto.flag.CreateFeatureFlagRequest;
import com.flagforge.presentation.dto.flag.FeatureFlagResponse;
import com.flagforge.presentation.dto.flag.ToggleFeatureFlagRequest;
import com.flagforge.presentation.dto.flag.UpdateFeatureFlagRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** REST adapter for flag CRUD/toggle — delegates to command/query handlers. */
@RestController
@RequestMapping("/api/v1/flags")
@RequiredArgsConstructor
@Tag(name = "Feature Flags", description = "CQRS-backed feature flag management API")
@SecurityRequirement(name = "bearerAuth")
public class FeatureFlagController {

	private final FeatureFlagCommandHandler commandHandler;
	private final FeatureFlagQueryHandler queryHandler;

	@PostMapping
	@Operation(summary = "Create a feature flag")
	public ResponseEntity<FeatureFlagResponse> create(@Valid @RequestBody CreateFeatureFlagRequest request) {
		UUID actorId = SecurityUtils.requireCurrentUserId();
		FeatureFlag created = commandHandler.handle(new CreateFeatureFlagCommand(
				request.key(),
				request.name(),
				request.description(),
				request.flagType(),
				request.environment(),
				request.defaultValue(),
				request.percentage(),
				request.rulesJson(),
				actorId
		));
		return ResponseEntity.status(HttpStatus.CREATED).body(FeatureFlagResponse.from(created));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get feature flag by id")
	public FeatureFlagResponse getById(@PathVariable UUID id) {
		return FeatureFlagResponse.from(queryHandler.handle(new GetFeatureFlagQuery(id)));
	}

	@GetMapping("/by-key/{key}")
	@Operation(summary = "Get feature flag by key and environment")
	public FeatureFlagResponse getByKey(
			@PathVariable String key,
			@RequestParam Environment environment
	) {
		return FeatureFlagResponse.from(
				queryHandler.handle(new GetFeatureFlagByKeyQuery(key, environment))
		);
	}

	@GetMapping
	@Operation(summary = "List feature flags (cursor pagination)")
	public CursorPageResponse<FeatureFlagResponse> list(
			@RequestParam(required = false) String cursor,
			@RequestParam(required = false) Integer limit,
			@RequestParam(required = false) Environment environment,
			@RequestParam(required = false) FlagStatus status
	) {
		CursorPageRequest pageRequest = CursorPageRequest.of(cursor, limit);
		CursorPage<FeatureFlag> page = queryHandler.handle(
				new ListFeatureFlagsQuery(pageRequest, environment, status)
		);
		return CursorPageResponse.from(page, FeatureFlagResponse::from);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a feature flag")
	public FeatureFlagResponse update(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateFeatureFlagRequest request
	) {
		UUID actorId = SecurityUtils.requireCurrentUserId();
		FeatureFlag updated = commandHandler.handle(new UpdateFeatureFlagCommand(
				id,
				request.name(),
				request.description(),
				request.flagType(),
				request.status(),
				request.defaultValue(),
				request.percentage(),
				request.rulesJson(),
				actorId
		));
		return FeatureFlagResponse.from(updated);
	}

	@PostMapping("/{id}/toggle")
	@Operation(summary = "Enable or disable a feature flag")
	public FeatureFlagResponse toggle(
			@PathVariable UUID id,
			@Valid @RequestBody ToggleFeatureFlagRequest request
	) {
		UUID actorId = SecurityUtils.requireCurrentUserId();
		FeatureFlag toggled = commandHandler.handle(new ToggleFeatureFlagCommand(
				id,
				request.enabled(),
				actorId
		));
		return FeatureFlagResponse.from(toggled);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a feature flag")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		UUID actorId = SecurityUtils.requireCurrentUserId();
		commandHandler.handle(new DeleteFeatureFlagCommand(id, actorId));
		return ResponseEntity.noContent().build();
	}
}
