package com.flagforge.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flagforge.domain.event.DomainEvent;
import com.flagforge.domain.repository.DomainEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Default publisher — logs events (no Kafka required).
 */
@Component
@ConditionalOnProperty(name = "flagforge.messaging.type", havingValue = "logging", matchIfMissing = true)
@Slf4j
public class LoggingDomainEventPublisher implements DomainEventPublisher {

	private final ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	@Override
	public void publish(DomainEvent event) {
		try {
			String json = objectMapper.writeValueAsString(DomainEventEnvelope.from(event));
			log.info("DomainEvent [{}] aggregate={}:{} payload={}",
					event.eventType(),
					event.aggregateType(),
					event.aggregateId(),
					json);
		} catch (JsonProcessingException ex) {
			log.info("DomainEvent [{}] aggregate={}:{}",
					event.eventType(),
					event.aggregateType(),
					event.aggregateId());
		}
	}

	@Override
	public String backend() {
		return "logging";
	}
}
