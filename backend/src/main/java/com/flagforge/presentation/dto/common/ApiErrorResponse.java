package com.flagforge.presentation.dto.common;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
		Instant timestamp,
		int status,
		String error,
		String message,
		String path,
		List<FieldError> fieldErrors
) {

	public record FieldError(String field, String message) {
	}

	public static ApiErrorResponse of(int status, String error, String message, String path) {
		return new ApiErrorResponse(Instant.now(), status, error, message, path, List.of());
	}

	public static ApiErrorResponse of(
			int status,
			String error,
			String message,
			String path,
			List<FieldError> fieldErrors
	) {
		return new ApiErrorResponse(Instant.now(), status, error, message, path, fieldErrors);
	}
}
