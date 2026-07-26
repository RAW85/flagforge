package com.flagforge.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flagforge.domain.event.DomainEvent;
import com.flagforge.domain.repository.DomainEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka {@link DomainEventPublisher}. Routes flag events and rollout events to separate topics.
 */
@Component
@ConditionalOnProperty(name = "flagforge.messaging.type", havingValue = "kafka")
@Slf4j
public class KafkaDomainEventPublisher implements DomainEventPublisher {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;
	private final String flagsTopic;
	private final String rolloutsTopic;

	public KafkaDomainEventPublisher(
			KafkaTemplate<String, String> kafkaTemplate,
			@org.springframework.beans.factory.annotation.Qualifier("kafkaObjectMapper") ObjectMapper objectMapper,
			@Value("${flagforge.kafka.topics.flags:flagforge.flags}") String flagsTopic,
			@Value("${flagforge.kafka.topics.rollouts:flagforge.rollouts}") String rolloutsTopic
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
		this.flagsTopic = flagsTopic;
		this.rolloutsTopic = rolloutsTopic;
	}

	@Override
	public void publish(DomainEvent event) {
		String topic = resolveTopic(event.aggregateType());
		String key = event.aggregateId() != null ? event.aggregateId().toString() : event.eventType();
		try {
			String json = objectMapper.writeValueAsString(DomainEventEnvelope.from(event));
			kafkaTemplate.send(topic, key, json);
			log.debug("Published {} to topic {} key={}", event.eventType(), topic, key);
		} catch (JsonProcessingException ex) {
			log.error("Failed to serialize domain event {}", event.eventType(), ex);
			throw new IllegalStateException("Failed to publish domain event", ex);
		}
	}

	@Override
	public String backend() {
		return "kafka";
	}

	private String resolveTopic(String aggregateType) {
		if ("RolloutSaga".equals(aggregateType)) {
			return rolloutsTopic;
		}
		return flagsTopic;
	}
}
