package com.flagforge.application.saga;

import java.util.UUID;

public record AdvanceRolloutSagaCommand(
		UUID sagaId,
		UUID actorId
) {
}
