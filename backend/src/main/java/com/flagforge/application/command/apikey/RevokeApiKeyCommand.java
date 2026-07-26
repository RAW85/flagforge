package com.flagforge.application.command.apikey;

import java.util.UUID;

public record RevokeApiKeyCommand(
		UUID id,
		UUID actorId
) {
}
