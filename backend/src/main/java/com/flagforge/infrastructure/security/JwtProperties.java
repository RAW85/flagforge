package com.flagforge.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "flagforge.security.jwt")
public record JwtProperties(
		String secret,
		long expirationMs
) {
}
