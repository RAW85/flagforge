package com.flagforge.presentation.dto.saga;

import com.flagforge.domain.flag.Environment;
import com.flagforge.domain.saga.RolloutSaga;
import com.flagforge.domain.saga.SagaStatus;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public record RolloutSagaResponse(
		UUID id,
		UUID flagId,
		String flagKey,
		Environment environment,
		SagaStatus status,
		int currentStepIndex,
		int currentPercentage,
		List<Integer> steps,
		boolean hasNextStep,
		UUID startedBy,
		Instant createdAt,
		Instant updatedAt,
		Instant completedAt,
		String failureReason
) {

	public static RolloutSagaResponse from(RolloutSaga saga) {
		List<Integer> steps = Arrays.stream(saga.steps()).boxed().toList();
		return new RolloutSagaResponse(
				saga.getId(),
				saga.getFlagId(),
				saga.getFlagKey(),
				saga.getEnvironment(),
				saga.getStatus(),
				saga.getCurrentStepIndex(),
				saga.currentPercentage(),
				steps,
				saga.hasNextStep(),
				saga.getStartedBy(),
				saga.getCreatedAt(),
				saga.getUpdatedAt(),
				saga.getCompletedAt(),
				saga.getFailureReason()
		);
	}
}
