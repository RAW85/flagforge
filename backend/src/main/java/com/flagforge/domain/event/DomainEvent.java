package com.flagforge.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base contract for all FlagForge domain events.
 */
public interface DomainEvent {

	UUID eventId();

	String eventType();

	Instant occurredAt();

	String aggregateType();

	UUID aggregateId();
}
