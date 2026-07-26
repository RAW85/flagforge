package com.flagforge.application.saga;

import com.flagforge.domain.evaluation.FlagSnapshot;
import com.flagforge.domain.event.FeatureFlagDomainEvent;
import com.flagforge.domain.event.RolloutSagaDomainEvent;
import com.flagforge.domain.exception.BusinessRuleException;
import com.flagforge.domain.exception.ConflictException;
import com.flagforge.domain.exception.ResourceNotFoundException;
import com.flagforge.domain.flag.FeatureFlag;
import com.flagforge.domain.flag.FlagStatus;
import com.flagforge.domain.flag.FlagType;
import com.flagforge.domain.repository.DomainEventPublisher;
import com.flagforge.domain.repository.FeatureFlagRepository;
import com.flagforge.domain.repository.FlagCache;
import com.flagforge.domain.repository.RolloutSagaRepository;
import com.flagforge.domain.saga.RolloutSaga;
import com.flagforge.domain.saga.SagaStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Progressive percentage rollout orchestration.
 * <p>
 * Start applies the first step; advance moves to the next; rollback disables the flag
 * and restores the first step percentage. Only one RUNNING saga per flag.
 */
@Service
@RequiredArgsConstructor
public class RolloutSagaCommandHandler {

	/** Default canary ladder when the client omits steps. */
	public static final List<Integer> DEFAULT_STEPS = List.of(0, 10, 25, 50, 100);

	private final RolloutSagaRepository rolloutSagaRepository;
	private final FeatureFlagRepository featureFlagRepository;
	private final FlagCache flagCache;
	private final DomainEventPublisher eventPublisher;

	/** Starts a saga on a PERCENTAGE flag; applies step 0 immediately and publishes STARTED. */
	@Transactional
	public RolloutSaga handle(StartRolloutSagaCommand command) {
		FeatureFlag flag = featureFlagRepository.findById(command.flagId())
				.orElseThrow(() -> ResourceNotFoundException.of("FeatureFlag", command.flagId()));

		if (flag.getFlagType() != FlagType.PERCENTAGE) {
			throw new BusinessRuleException("Rollout sagas require a PERCENTAGE feature flag");
		}
		if (rolloutSagaRepository.existsByFlagIdAndStatus(flag.getId(), SagaStatus.RUNNING)) {
			throw new ConflictException("A rollout saga is already running for flag " + flag.getKey());
		}

		List<Integer> steps = normalizeSteps(command.steps());
		String stepsCsv = steps.stream().map(String::valueOf).collect(Collectors.joining(","));

		// Apply first step immediately
		int initialPercentage = steps.getFirst();
		applyPercentage(flag, initialPercentage, command.actorId());

		RolloutSaga saga = RolloutSaga.builder()
				.flagId(flag.getId())
				.flagKey(flag.getKey())
				.environment(flag.getEnvironment())
				.status(SagaStatus.RUNNING)
				.currentStepIndex(0)
				.stepsCsv(stepsCsv)
				.startedBy(command.actorId())
				.build();

		RolloutSaga saved = rolloutSagaRepository.save(saga);

		eventPublisher.publish(RolloutSagaDomainEvent.of(
				RolloutSagaDomainEvent.STARTED,
				saved.getId(),
				flag.getId(),
				flag.getKey(),
				flag.getEnvironment(),
				saved.getStatus(),
				saved.getCurrentStepIndex(),
				initialPercentage,
				initialPercentage,
				command.actorId(),
				"steps=" + stepsCsv
		));

		return saved;
	}

	/** Moves to the next percentage step (or completes if already at the last step). */
	@Transactional
	public RolloutSaga handle(AdvanceRolloutSagaCommand command) {
		RolloutSaga saga = requireRunningSaga(command.sagaId());
		FeatureFlag flag = featureFlagRepository.findById(saga.getFlagId())
				.orElseThrow(() -> ResourceNotFoundException.of("FeatureFlag", saga.getFlagId()));

		if (!saga.hasNextStep()) {
			return completeSaga(saga, flag, command.actorId());
		}

		int nextPercentage = saga.nextPercentage();
		saga.setCurrentStepIndex(saga.getCurrentStepIndex() + 1);
		applyPercentage(flag, nextPercentage, command.actorId());

		if (!saga.hasNextStep() && nextPercentage >= 100) {
			saga.setStatus(SagaStatus.COMPLETED);
			saga.setCompletedAt(Instant.now());
		}

		RolloutSaga saved = rolloutSagaRepository.save(saga);

		String eventType = saved.getStatus() == SagaStatus.COMPLETED
				? RolloutSagaDomainEvent.COMPLETED
				: RolloutSagaDomainEvent.STEP_ADVANCED;

		eventPublisher.publish(RolloutSagaDomainEvent.of(
				eventType,
				saved.getId(),
				flag.getId(),
				flag.getKey(),
				flag.getEnvironment(),
				saved.getStatus(),
				saved.getCurrentStepIndex(),
				nextPercentage,
				nextPercentage,
				command.actorId(),
				null
		));

		return saved;
	}

	/**
	 * Compensating action: set percentage to first step, disable flag, mark saga ROLLED_BACK.
	 * Allowed from RUNNING or COMPLETED.
	 */
	@Transactional
	public RolloutSaga handle(RollbackRolloutSagaCommand command) {
		RolloutSaga saga = rolloutSagaRepository.findById(command.sagaId())
				.orElseThrow(() -> ResourceNotFoundException.of("RolloutSaga", command.sagaId()));

		if (saga.getStatus() != SagaStatus.RUNNING && saga.getStatus() != SagaStatus.COMPLETED) {
			throw new BusinessRuleException("Cannot rollback saga in status " + saga.getStatus());
		}

		FeatureFlag flag = featureFlagRepository.findById(saga.getFlagId())
				.orElseThrow(() -> ResourceNotFoundException.of("FeatureFlag", saga.getFlagId()));

		// Compensating action: disable flag and set percentage to first step (usually 0)
		int rollbackPercentage = saga.steps()[0];
		flag.setPercentage(rollbackPercentage);
		flag.setEnabled(false);
		FeatureFlag savedFlag = featureFlagRepository.save(flag);
		flagCache.put(FlagSnapshot.from(savedFlag));

		eventPublisher.publish(FeatureFlagDomainEvent.of(
				FeatureFlagDomainEvent.ROLLOUT_PERCENTAGE_CHANGED,
				savedFlag.getId(),
				savedFlag.getKey(),
				savedFlag.getEnvironment(),
				savedFlag.isEnabled(),
				savedFlag.getStatus(),
				savedFlag.getFlagType(),
				savedFlag.getPercentage(),
				command.actorId()
		));

		saga.setStatus(SagaStatus.ROLLED_BACK);
		saga.setCompletedAt(Instant.now());
		saga.setFailureReason(command.reason() != null ? command.reason() : "Manual rollback");
		RolloutSaga saved = rolloutSagaRepository.save(saga);

		eventPublisher.publish(RolloutSagaDomainEvent.of(
				RolloutSagaDomainEvent.ROLLED_BACK,
				saved.getId(),
				flag.getId(),
				flag.getKey(),
				flag.getEnvironment(),
				saved.getStatus(),
				saved.getCurrentStepIndex(),
				rollbackPercentage,
				null,
				command.actorId(),
				saved.getFailureReason()
		));

		return saved;
	}

	private RolloutSaga completeSaga(RolloutSaga saga, FeatureFlag flag, java.util.UUID actorId) {
		saga.setStatus(SagaStatus.COMPLETED);
		saga.setCompletedAt(Instant.now());
		RolloutSaga saved = rolloutSagaRepository.save(saga);
		eventPublisher.publish(RolloutSagaDomainEvent.of(
				RolloutSagaDomainEvent.COMPLETED,
				saved.getId(),
				flag.getId(),
				flag.getKey(),
				flag.getEnvironment(),
				saved.getStatus(),
				saved.getCurrentStepIndex(),
				saga.currentPercentage(),
				saga.currentPercentage(),
				actorId,
				"already at final step"
		));
		return saved;
	}

	/** Writes percentage to the flag, enables it, warms cache, publishes domain event. */
	private void applyPercentage(FeatureFlag flag, int percentage, java.util.UUID actorId) {
		flag.setPercentage(percentage);
		flag.setEnabled(true);
		if (flag.getStatus() == FlagStatus.DRAFT) {
			flag.setStatus(FlagStatus.ACTIVE);
		}
		FeatureFlag saved = featureFlagRepository.save(flag);
		flagCache.put(FlagSnapshot.from(saved));

		eventPublisher.publish(FeatureFlagDomainEvent.of(
				FeatureFlagDomainEvent.ROLLOUT_PERCENTAGE_CHANGED,
				saved.getId(),
				saved.getKey(),
				saved.getEnvironment(),
				saved.isEnabled(),
				saved.getStatus(),
				saved.getFlagType(),
				saved.getPercentage(),
				actorId
		));
	}

	private RolloutSaga requireRunningSaga(java.util.UUID sagaId) {
		RolloutSaga saga = rolloutSagaRepository.findById(sagaId)
				.orElseThrow(() -> ResourceNotFoundException.of("RolloutSaga", sagaId));
		if (saga.getStatus() != SagaStatus.RUNNING) {
			throw new BusinessRuleException("Saga is not running (status=" + saga.getStatus() + ")");
		}
		return saga;
	}

	private List<Integer> normalizeSteps(List<Integer> input) {
		List<Integer> steps = (input == null || input.isEmpty()) ? DEFAULT_STEPS : input;
		if (steps.size() < 2) {
			throw new BusinessRuleException("Rollout steps must contain at least 2 percentages");
		}
		int previous = -1;
		for (Integer step : steps) {
			if (step == null || step < 0 || step > 100) {
				throw new BusinessRuleException("Each rollout step must be between 0 and 100");
			}
			if (step < previous) {
				throw new BusinessRuleException("Rollout steps must be non-decreasing");
			}
			previous = step;
		}
		return List.copyOf(steps);
	}
}
