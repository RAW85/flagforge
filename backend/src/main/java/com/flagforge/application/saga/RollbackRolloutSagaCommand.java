package com.flagforge.application.saga;

import java.util.UUID;

public record RollbackRolloutSagaCommand(
		UUID sagaId,
		UUID actorId,
		String reason
) {
}
