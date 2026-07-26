package com.flagforge.domain.exception;

/**
 * Base type for domain / application rule violations.
 */
public abstract class DomainException extends RuntimeException {

	protected DomainException(String message) {
		super(message);
	}

	protected DomainException(String message, Throwable cause) {
		super(message, cause);
	}
}
