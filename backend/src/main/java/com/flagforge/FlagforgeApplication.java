package com.flagforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FlagForge entry point.
 * <p>
 * Layers: {@code domain} → {@code application} (CQRS/sagas) →
 * {@code infrastructure} (JPA, cache, messaging, security) → {@code presentation} (REST).
 */
@SpringBootApplication
public class FlagforgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlagforgeApplication.class, args);
	}

}
