package com.flagforge.presentation.exception;

import com.flagforge.domain.exception.BusinessRuleException;
import com.flagforge.domain.exception.ConflictException;
import com.flagforge.domain.exception.DomainException;
import com.flagforge.domain.exception.ResourceNotFoundException;
import com.flagforge.domain.exception.UnauthorizedException;
import com.flagforge.presentation.dto.common.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/** Maps domain and validation failures to {@link ApiErrorResponse} HTTP bodies. */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ApiErrorResponse> handleUnauthorized(
			UnauthorizedException ex,
			HttpServletRequest request
	) {
		return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiErrorResponse> handleAccessDenied(
			AccessDeniedException ex,
			HttpServletRequest request
	) {
		return build(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(
			ResourceNotFoundException ex,
			HttpServletRequest request
	) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiErrorResponse> handleConflict(
			ConflictException ex,
			HttpServletRequest request
	) {
		return build(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(BusinessRuleException.class)
	public ResponseEntity<ApiErrorResponse> handleBusinessRule(
			BusinessRuleException ex,
			HttpServletRequest request
	) {
		return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ApiErrorResponse> handleDomain(
			DomainException ex,
			HttpServletRequest request
	) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
			IllegalArgumentException ex,
			HttpServletRequest request
	) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(
			MethodArgumentNotValidException ex,
			HttpServletRequest request
	) {
		List<ApiErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(this::toFieldError)
				.toList();

		ApiErrorResponse body = ApiErrorResponse.of(
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				"Validation failed",
				request.getRequestURI(),
				fieldErrors
		);
		return ResponseEntity.badRequest().body(body);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNoResource(
			NoResourceFoundException ex,
			HttpServletRequest request
	) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleGeneric(
			Exception ex,
			HttpServletRequest request
	) {
		// Log root cause so issues like H2 console / Tomcat temp are visible in the backend window
		org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class)
				.error("Unhandled error on {}", request.getRequestURI(), ex);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request.getRequestURI());
	}

	private ApiErrorResponse.FieldError toFieldError(FieldError error) {
		return new ApiErrorResponse.FieldError(error.getField(), error.getDefaultMessage());
	}

	private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, String path) {
		return ResponseEntity.status(status)
				.body(ApiErrorResponse.of(status.value(), status.getReasonPhrase(), message, path));
	}
}
