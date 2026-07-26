package com.flagforge.infrastructure.messaging;

import com.flagforge.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Wire format for Kafka / logs — wraps a typed domain event payload as JSON-friendly map fields.
 */
public record DomainEventEnvelope(
		UUID eventId,
		String eventType,
		String aggregateType,
		UUID aggregateId,
		Instant occurredAt,
		Object payload
) {

	public static DomainEventEnvelope from(DomainEvent event) {
		return new DomainEventEnvelope(
				event.eventId(),
				event.eventType(),
				event.aggregateType(),
				event.aggregateId(),
				event.occurredAt(),
				event
		);
	}
}
