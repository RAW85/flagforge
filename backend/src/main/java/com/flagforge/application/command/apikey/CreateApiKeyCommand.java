package com.flagforge.application.command.apikey;

import com.flagforge.domain.flag.Environment;

import java.util.UUID;

/**
 * @param environmentScope optional; {@code null} allows all environments
 * @param ownerId          user who owns the key (usually the creator)
 */
public record CreateApiKeyCommand(
		String name,
		Environment environmentScope,
		UUID ownerId
) {
}
