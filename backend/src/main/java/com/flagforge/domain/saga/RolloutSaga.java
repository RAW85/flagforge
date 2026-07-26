package com.flagforge.domain.saga;

import com.flagforge.domain.common.BaseEntity;
import com.flagforge.domain.flag.Environment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Progressive rollout saga for a percentage-based feature flag.
 * <p>
 * Steps are stored as a CSV of percentages, e.g. {@code "0,10,25,50,100"}.
 */
@Entity
@Table(
		name = "rollout_sagas",
		indexes = {
				@Index(name = "idx_rollout_sagas_flag_id", columnList = "flagId"),
				@Index(name = "idx_rollout_sagas_status", columnList = "status")
		}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolloutSaga extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private UUID flagId;

	@Column(nullable = false, length = 128)
	private String flagKey;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private Environment environment;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	@Builder.Default
	private SagaStatus status = SagaStatus.RUNNING;

	/**
	 * Zero-based index into {@link #stepsCsv}.
	 */
	@Column(nullable = false)
	@Builder.Default
	private int currentStepIndex = 0;

	/**
	 * Comma-separated percentage steps, e.g. {@code 0,10,25,50,100}.
	 */
	@Column(nullable = false, length = 255)
	private String stepsCsv;

	@Column(nullable = false)
	private UUID startedBy;

	@Column
	private Instant completedAt;

	@Column(length = 1000)
	private String failureReason;

	/** Parsed percentage ladder from {@link #stepsCsv}. */
	public int[] steps() {
		String[] parts = stepsCsv.split(",");
		int[] steps = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			steps[i] = Integer.parseInt(parts[i].trim());
		}
		return steps;
	}

	/** Percentage at {@link #currentStepIndex}. */
	public int currentPercentage() {
		int[] steps = steps();
		if (currentStepIndex < 0 || currentStepIndex >= steps.length) {
			return steps[steps.length - 1];
		}
		return steps[currentStepIndex];
	}

	public boolean hasNextStep() {
		return currentStepIndex + 1 < steps().length;
	}

	/** Percentage of the next step (does not advance index). */
	public int nextPercentage() {
		if (!hasNextStep()) {
			throw new IllegalStateException("Saga has no next step");
		}
		return steps()[currentStepIndex + 1];
	}
}
