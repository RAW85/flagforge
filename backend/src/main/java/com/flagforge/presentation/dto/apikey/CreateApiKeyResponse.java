package com.flagforge.presentation.dto.apikey;

import com.flagforge.application.command.apikey.CreateApiKeyResult;

/**
 * Returned only from create — includes the raw secret once.
 */
public record CreateApiKeyResponse(
		ApiKeyResponse apiKey,
		String rawKey,
		String warning
) {

	public static CreateApiKeyResponse from(CreateApiKeyResult result) {
		return new CreateApiKeyResponse(
				ApiKeyResponse.from(result.apiKey()),
				result.rawKey(),
				"Store this API key securely. It will not be shown again."
		);
	}
}
