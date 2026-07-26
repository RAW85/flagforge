package com.flagforge.presentation.rest;

import com.flagforge.application.saga.AdvanceRolloutSagaCommand;
import com.flagforge.application.saga.GetRolloutSagaQuery;
import com.flagforge.application.saga.ListRolloutSagasByFlagQuery;
import com.flagforge.application.saga.RollbackRolloutSagaCommand;
import com.flagforge.application.saga.RolloutSagaCommandHandler;
import com.flagforge.application.saga.RolloutSagaQueryHandler;
import com.flagforge.application.saga.StartRolloutSagaCommand;
import com.flagforge.domain.saga.RolloutSaga;
import com.flagforge.infrastructure.security.SecurityUtils;
import com.flagforge.presentation.dto.saga.RollbackRolloutSagaRequest;
import com.flagforge.presentation.dto.saga.RolloutSagaResponse;
import com.flagforge.presentation.dto.saga.StartRolloutSagaRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Start / advance / rollback progressive percentage rollouts. */
@RestController
@RequestMapping("/api/v1/rollouts")
@RequiredArgsConstructor
@Tag(name = "Rollout Sagas", description = "Progressive feature rollout workflows")
@SecurityRequirement(name = "bearerAuth")
public class RolloutSagaController {

	private final RolloutSagaCommandHandler commandHandler;
	private final RolloutSagaQueryHandler queryHandler;

	@PostMapping
	@Operation(summary = "Start a progressive rollout saga for a PERCENTAGE flag")
	public ResponseEntity<RolloutSagaResponse> start(@Valid @RequestBody StartRolloutSagaRequest request) {
		UUID actorId = SecurityUtils.requireCurrentUserId();
		RolloutSaga saga = commandHandler.handle(new StartRolloutSagaCommand(
				request.flagId(),
				request.steps(),
				actorId
		));
		return ResponseEntity.status(HttpStatus.CREATED).body(RolloutSagaResponse.from(saga));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get rollout saga by id")
	public RolloutSagaResponse get(@PathVariable UUID id) {
		return RolloutSagaResponse.from(queryHandler.handle(new GetRolloutSagaQuery(id)));
	}

	@GetMapping
	@Operation(summary = "List rollout sagas for a flag")
	public List<RolloutSagaResponse> listByFlag(@RequestParam UUID flagId) {
		return queryHandler.handle(new ListRolloutSagasByFlagQuery(flagId)).stream()
				.map(RolloutSagaResponse::from)
				.toList();
	}

	@PostMapping("/{id}/advance")
	@Operation(summary = "Advance saga to the next percentage step")
	public RolloutSagaResponse advance(@PathVariable UUID id) {
		UUID actorId = SecurityUtils.requireCurrentUserId();
		RolloutSaga saga = commandHandler.handle(new AdvanceRolloutSagaCommand(id, actorId));
		return RolloutSagaResponse.from(saga);
	}

	@PostMapping("/{id}/rollback")
	@Operation(summary = "Rollback saga (disable flag, reset percentage)")
	public RolloutSagaResponse rollback(
			@PathVariable UUID id,
			@RequestBody(required = false) RollbackRolloutSagaRequest request
	) {
		UUID actorId = SecurityUtils.requireCurrentUserId();
		String reason = request != null ? request.reason() : null;
		RolloutSaga saga = commandHandler.handle(new RollbackRolloutSagaCommand(id, actorId, reason));
		return RolloutSagaResponse.from(saga);
	}
}
