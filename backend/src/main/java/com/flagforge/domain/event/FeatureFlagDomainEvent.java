package com.flagforge.domain.event;

import com.flagforge.domain.flag.Environment;
import com.flagforge.domain.flag.FlagStatus;
import com.flagforge.domain.flag.FlagType;

import java.time.Instant;
import java.util.UUID;

/**
 * Events emitted when a feature flag changes.
 */
public record FeatureFlagDomainEvent(
		UUID eventId,
		String eventType,
		Instant occurredAt,
		UUID flagId,
		String flagKey,
		Environment environment,
		boolean enabled,
		FlagStatus status,
		FlagType flagType,
		Integer percentage,
		UUID actorId
) implements DomainEvent {

	public static final String CREATED = "FeatureFlagCreated";
	public static final String UPDATED = "FeatureFlagUpdated";
	public static final String TOGGLED = "FeatureFlagToggled";
	public static final String DELETED = "FeatureFlagDeleted";
	public static final String ROLLOUT_PERCENTAGE_CHANGED = "FeatureFlagRolloutPercentageChanged";

	@Override
	public String aggregateType() {
		return "FeatureFlag";
	}

	@Override
	public UUID aggregateId() {
		return flagId;
	}

	public static FeatureFlagDomainEvent of(
			String eventType,
			UUID flagId,
			String flagKey,
			Environment environment,
			boolean enabled,
			FlagStatus status,
			FlagType flagType,
			Integer percentage,
			UUID actorId
	) {
		return new FeatureFlagDomainEvent(
				UUID.randomUUID(),
				eventType,
				Instant.now(),
				flagId,
				flagKey,
				environment,
				enabled,
				status,
				flagType,
				percentage,
				actorId
		);
	}
}
