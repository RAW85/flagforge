package com.flagforge.domain.flag;

import com.flagforge.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Core feature flag aggregate. Evaluation details live in Redis at runtime;
 * this entity is the source of truth persisted in the database.
 */
@Entity
@Table(
		name = "feature_flags",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_feature_flags_key_environment",
						columnNames = {"flag_key", "environment"}
				)
		},
		indexes = {
				@Index(name = "idx_feature_flags_status", columnList = "status"),
				@Index(name = "idx_feature_flags_enabled", columnList = "enabled")
		}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureFlag extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	/**
	 * Stable machine-readable key used by SDKs (e.g. "new-checkout-flow").
	 */
	@Column(name = "flag_key", nullable = false, length = 128)
	private String key;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(length = 2000)
	private String description;

	@Column(nullable = false)
	@Builder.Default
	private boolean enabled = false;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	@Builder.Default
	private FlagStatus status = FlagStatus.DRAFT;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	@Builder.Default
	private FlagType flagType = FlagType.BOOLEAN;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	@Builder.Default
	private Environment environment = Environment.DEVELOPMENT;

	/**
	 * Default evaluation result when no targeting rules match.
	 * Stored as string for flexibility (e.g. "true", "false", variant name, JSON).
	 */
	@Column(nullable = false, length = 512)
	@Builder.Default
	private String defaultValue = "false";

	/**
	 * Rollout percentage for {@link FlagType#PERCENTAGE} flags (0–100).
	 */
	@Column
	private Integer percentage;

	/**
	 * Optional JSON payload for multivariate variants / targeting rules.
	 * Will be refined into dedicated tables/value objects in later steps.
	 */
	@Column(columnDefinition = "TEXT")
	private String rulesJson;

	@Column(nullable = false)
	private UUID createdBy;
}
