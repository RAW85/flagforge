package com.flagforge.presentation.rest;

import com.flagforge.application.query.evaluation.EvaluateFlagQuery;
import com.flagforge.application.query.evaluation.EvaluateFlagQueryHandler;
import com.flagforge.domain.evaluation.EvaluationResult;
import com.flagforge.domain.exception.BusinessRuleException;
import com.flagforge.domain.flag.Environment;
import com.flagforge.infrastructure.security.ApiKeyPrincipal;
import com.flagforge.presentation.dto.evaluation.EvaluateFlagRequest;
import com.flagforge.presentation.dto.evaluation.EvaluateFlagResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public SDK evaluation surface — authenticate with {@code X-API-Key}.
 */
@RestController
@RequestMapping("/api/v1/sdk")
@RequiredArgsConstructor
@Tag(name = "SDK", description = "Public evaluation API for client SDKs (API key auth)")
@SecurityRequirement(name = "apiKeyAuth")
public class SdkEvaluationController {

	private final EvaluateFlagQueryHandler evaluateFlagQueryHandler;

	@PostMapping("/evaluate")
	@Operation(summary = "Evaluate a flag using an SDK API key")
	public EvaluateFlagResponse evaluate(@Valid @RequestBody EvaluateFlagRequest request) {
		assertEnvironmentAllowed(request.environment());
		EvaluationResult result = evaluateFlagQueryHandler.handle(new EvaluateFlagQuery(
				request.flagKey(),
				request.environment(),
				request.subjectId(),
				request.contextJson(),
				Boolean.TRUE.equals(request.record())
		));
		return EvaluateFlagResponse.from(result);
	}

	@GetMapping("/evaluate/{flagKey}")
	@Operation(summary = "Evaluate a flag (GET) using an SDK API key")
	public EvaluateFlagResponse evaluateGet(
			@PathVariable String flagKey,
			@RequestParam Environment environment,
			@RequestParam String subjectId,
			@RequestParam(required = false) Boolean record
	) {
		assertEnvironmentAllowed(environment);
		EvaluationResult result = evaluateFlagQueryHandler.handle(new EvaluateFlagQuery(
				flagKey,
				environment,
				subjectId,
				null,
				Boolean.TRUE.equals(record)
		));
		return EvaluateFlagResponse.from(result);
	}

	/** Rejects evaluate calls when the API key is scoped to a different environment. */
	private void assertEnvironmentAllowed(Environment requested) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof ApiKeyPrincipal principal)) {
			return;
		}
		Environment scope = principal.environmentScope();
		if (scope != null && scope != requested) {
			throw new BusinessRuleException(
					"API key is scoped to " + scope + " and cannot evaluate " + requested
			);
		}
	}
}
