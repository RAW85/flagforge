package com.flagforge.presentation.rest;

import com.flagforge.application.query.evaluation.EvaluateFlagQuery;
import com.flagforge.application.query.evaluation.EvaluateFlagQueryHandler;
import com.flagforge.domain.evaluation.EvaluationResult;
import com.flagforge.domain.flag.Environment;
import com.flagforge.presentation.dto.evaluation.EvaluateFlagRequest;
import com.flagforge.presentation.dto.evaluation.EvaluateFlagResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard evaluation API — JWT auth ({@code VIEWER}+). Same engine as the SDK path.
 */
@RestController
@RequestMapping("/api/v1/evaluate")
@RequiredArgsConstructor
@Tag(name = "Evaluation", description = "Fast feature flag evaluation (cache-backed)")
@SecurityRequirement(name = "bearerAuth")
public class EvaluationController {

	private final EvaluateFlagQueryHandler evaluateFlagQueryHandler;

	@PostMapping
	@Operation(summary = "Evaluate a feature flag for a subject")
	public EvaluateFlagResponse evaluate(@Valid @RequestBody EvaluateFlagRequest request) {
		EvaluationResult result = evaluateFlagQueryHandler.handle(new EvaluateFlagQuery(
				request.flagKey(),
				request.environment(),
				request.subjectId(),
				request.contextJson(),
				Boolean.TRUE.equals(request.record())
		));
		return EvaluateFlagResponse.from(result);
	}

	@GetMapping("/{flagKey}")
	@Operation(summary = "Evaluate a feature flag (GET convenience for SDKs / debugging)")
	public EvaluateFlagResponse evaluateGet(
			@PathVariable String flagKey,
			@RequestParam Environment environment,
			@RequestParam String subjectId,
			@RequestParam(required = false) Boolean record
	) {
		EvaluationResult result = evaluateFlagQueryHandler.handle(new EvaluateFlagQuery(
				flagKey,
				environment,
				subjectId,
				null,
				Boolean.TRUE.equals(record)
		));
		return EvaluateFlagResponse.from(result);
	}
}
