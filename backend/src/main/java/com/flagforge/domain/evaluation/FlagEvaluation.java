package com.flagforge.domain.evaluation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable record of a single flag evaluation (analytics / debugging).
 * High-volume evaluations may later stream to Kafka and a cold store;
 * this table supports near-term history and audit of evaluation decisions.
 */
@Entity
@Table(
		name = "flag_evaluations",
		indexes = {
				@Index(name = "idx_flag_evaluations_flag_id", columnList = "flagId"),
				@Index(name = "idx_flag_evaluations_subject", columnList = "subjectId"),
				@Index(name = "idx_flag_evaluations_evaluated_at", columnList = "evaluatedAt")
		}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlagEvaluation {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private UUID flagId;

	@Column(nullable = false, length = 128)
	private String flagKey;

	/**
	 * End-user / subject identifier used for sticky bucketing.
	 */
	@Column(nullable = false, length = 255)
	private String subjectId;

	/**
	 * Evaluation context snapshot (attributes used for targeting), as JSON.
	 */
	@Column(columnDefinition = "TEXT")
	private String contextJson;

	/**
	 * Resolved value returned to the client (e.g. "true", variant key).
	 */
	@Column(nullable = false, length = 512)
	private String resultValue;

	/**
	 * Human-readable reason (DEFAULT, PERCENTAGE_BUCKET, RULE_MATCH, DISABLED, …).
	 */
	@Column(nullable = false, length = 128)
	private String reason;

	@Column(nullable = false, updatable = false)
	private Instant evaluatedAt;

	@PrePersist
	protected void onCreate() {
		if (this.evaluatedAt == null) {
			this.evaluatedAt = Instant.now();
		}
	}
}
