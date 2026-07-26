package com.flagforge.infrastructure.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes domain events from Kafka (audit trail / future projections).
 * Saga state is driven by the command side; this listener is the event-driven fan-out point.
 */
@Component
@ConditionalOnProperty(name = "flagforge.messaging.type", havingValue = "kafka")
@Slf4j
public class DomainEventKafkaListener {

	@KafkaListener(
			topics = "${flagforge.kafka.topics.flags:flagforge.flags}",
			groupId = "${spring.kafka.consumer.group-id:flagforge}"
	)
	public void onFlagEvent(String payload) {
		log.info("Kafka flag event received: {}", payload);
	}

	@KafkaListener(
			topics = "${flagforge.kafka.topics.rollouts:flagforge.rollouts}",
			groupId = "${spring.kafka.consumer.group-id:flagforge}"
	)
	public void onRolloutEvent(String payload) {
		log.info("Kafka rollout event received: {}", payload);
	}
}
