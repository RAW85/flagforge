package com.flagforge.domain.repository;

import com.flagforge.domain.event.DomainEvent;

/**
 * Port for publishing domain events (logging, Kafka, etc.).
 */
public interface DomainEventPublisher {

	void publish(DomainEvent event);

	String backend();
}
