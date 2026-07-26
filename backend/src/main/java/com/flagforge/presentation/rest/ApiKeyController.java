package com.flagforge.presentation.rest;

import com.flagforge.application.command.apikey.ApiKeyCommandHandler;
import com.flagforge.application.command.apikey.CreateApiKeyCommand;
import com.flagforge.application.command.apikey.CreateApiKeyResult;
import com.flagforge.application.command.apikey.RevokeApiKeyCommand;
import com.flagforge.application.query.apikey.ApiKeyQueryHandler;
import com.flagforge.application.query.apikey.ListApiKeysQuery;
import com.flagforge.domain.apikey.ApiKey;
import com.flagforge.infrastructure.security.AuthenticatedUser;
import com.flagforge.infrastructure.security.SecurityUtils;
import com.flagforge.domain.user.UserRole;
import com.flagforge.presentation.dto.apikey.ApiKeyResponse;
import com.flagforge.presentation.dto.apikey.CreateApiKeyRequest;
import com.flagforge.presentation.dto.apikey.CreateApiKeyResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Manage SDK credentials; create returns the raw secret once. */
@RestController
@RequestMapping("/api/v1/api-keys")
@RequiredArgsConstructor
@Tag(name = "API Keys", description = "SDK credentials for public flag evaluation")
@SecurityRequirement(name = "bearerAuth")
public class ApiKeyController {

	private final ApiKeyCommandHandler commandHandler;
	private final ApiKeyQueryHandler queryHandler;

	@PostMapping
	@Operation(summary = "Create an SDK API key (raw secret returned once)")
	public ResponseEntity<CreateApiKeyResponse> create(@Valid @RequestBody CreateApiKeyRequest request) {
		AuthenticatedUser user = SecurityUtils.requireCurrentUser();
		CreateApiKeyResult result = commandHandler.handle(new CreateApiKeyCommand(
				request.name(),
				request.environmentScope(),
				user.id()
		));
		return ResponseEntity.status(HttpStatus.CREATED).body(CreateApiKeyResponse.from(result));
	}

	@GetMapping
	@Operation(summary = "List API keys (admins see all; others see own keys)")
	public List<ApiKeyResponse> list() {
		AuthenticatedUser user = SecurityUtils.requireCurrentUser();
		UUID ownerFilter = user.role() == UserRole.ADMIN ? null : user.id();
		return queryHandler.handle(new ListApiKeysQuery(ownerFilter)).stream()
				.map(ApiKeyResponse::from)
				.toList();
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Revoke an API key")
	public ApiKeyResponse revoke(@PathVariable UUID id) {
		UUID actorId = SecurityUtils.requireCurrentUserId();
		ApiKey revoked = commandHandler.handle(new RevokeApiKeyCommand(id, actorId));
		return ApiKeyResponse.from(revoked);
	}
}
