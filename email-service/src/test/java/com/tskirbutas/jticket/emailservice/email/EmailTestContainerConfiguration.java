package com.tskirbutas.jticket.emailservice.email;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;

interface EmailTestContainerConfiguration {
	@Container
	static KafkaContainer kafka = new KafkaContainer("apache/kafka:3.8.0");

	@DynamicPropertySource
	static void kafkaProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
	}
}