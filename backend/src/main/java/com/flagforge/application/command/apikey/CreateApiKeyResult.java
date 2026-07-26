package com.flagforge.application.command.apikey;

import com.flagforge.domain.apikey.ApiKey;

/**
 * Includes the raw key once — never persisted again.
 */
public record CreateApiKeyResult(
		ApiKey apiKey,
		String rawKey
) {
}
