package com.flagforge.presentation.dto.apikey;

import com.flagforge.domain.apikey.ApiKey;
import com.flagforge.domain.flag.Environment;

import java.time.Instant;
import java.util.UUID;

public record ApiKeyResponse(
		UUID id,
		String name,
		String keyPrefix,
		String displayKey,
		UUID ownerId,
		Environment environmentScope,
		boolean active,
		Instant lastUsedAt,
		Instant revokedAt,
		Instant createdAt
) {

	public static ApiKeyResponse from(ApiKey key) {
		return new ApiKeyResponse(
				key.getId(),
				key.getName(),
				key.getKeyPrefix(),
				"ffk_" + key.getKeyPrefix() + "_••••••••",
				key.getOwnerId(),
				key.getEnvironmentScope(),
				key.isActive(),
				key.getLastUsedAt(),
				key.getRevokedAt(),
				key.getCreatedAt()
		);
	}
}
