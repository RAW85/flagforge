package com.flagforge.application.query.flag;

import com.flagforge.domain.common.CursorPageRequest;
import com.flagforge.domain.flag.Environment;
import com.flagforge.domain.flag.FlagStatus;

public record ListFeatureFlagsQuery(
		CursorPageRequest pageRequest,
		Environment environment,
		FlagStatus status
) {
}
