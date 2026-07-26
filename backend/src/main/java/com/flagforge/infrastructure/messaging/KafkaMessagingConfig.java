package com.flagforge.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
@ConditionalOnProperty(name = "flagforge.messaging.type", havingValue = "kafka")
public class KafkaMessagingConfig {

	@Bean
	ObjectMapper kafkaObjectMapper() {
		return new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	@Bean
	NewTopic flagsTopic(@Value("${flagforge.kafka.topics.flags:flagforge.flags}") String name) {
		return TopicBuilder.name(name).partitions(3).replicas(1).build();
	}

	@Bean
	NewTopic rolloutsTopic(@Value("${flagforge.kafka.topics.rollouts:flagforge.rollouts}") String name) {
		return TopicBuilder.name(name).partitions(3).replicas(1).build();
	}
}
