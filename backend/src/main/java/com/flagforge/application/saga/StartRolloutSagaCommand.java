package com.flagforge.application.saga;

import java.util.List;
import java.util.UUID;

public record StartRolloutSagaCommand(
		UUID flagId,
		List<Integer> steps,
		UUID actorId
) {
}
