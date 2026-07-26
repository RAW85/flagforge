package com.flagforge.domain.event;

import com.flagforge.domain.flag.Environment;
import com.flagforge.domain.saga.SagaStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Events emitted by progressive rollout sagas.
 */
public record RolloutSagaDomainEvent(
		UUID eventId,
		String eventType,
		Instant occurredAt,
		UUID sagaId,
		UUID flagId,
		String flagKey,
		Environment environment,
		SagaStatus status,
		int currentStepIndex,
		Integer currentPercentage,
		Integer targetPercentage,
		UUID actorId,
		String detail
) implements DomainEvent {

	public static final String STARTED = "RolloutSagaStarted";
	public static final String STEP_ADVANCED = "RolloutSagaStepAdvanced";
	public static final String COMPLETED = "RolloutSagaCompleted";
	public static final String ROLLED_BACK = "RolloutSagaRolledBack";
	public static final String FAILED = "RolloutSagaFailed";

	@Override
	public String aggregateType() {
		return "RolloutSaga";
	}

	@Override
	public UUID aggregateId() {
		return sagaId;
	}

	public static RolloutSagaDomainEvent of(
			String eventType,
			UUID sagaId,
			UUID flagId,
			String flagKey,
			Environment environment,
			SagaStatus status,
			int currentStepIndex,
			Integer currentPercentage,
			Integer targetPercentage,
			UUID actorId,
			String detail
	) {
		return new RolloutSagaDomainEvent(
				UUID.randomUUID(),
				eventType,
				Instant.now(),
				sagaId,
				flagId,
				flagKey,
				environment,
				status,
				currentStepIndex,
				currentPercentage,
				targetPercentage,
				actorId,
				detail
		);
	}
}
