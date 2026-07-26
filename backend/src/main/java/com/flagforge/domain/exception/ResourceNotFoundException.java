package com.flagforge.domain.exception;

public class ResourceNotFoundException extends DomainException {

	public ResourceNotFoundException(String message) {
		super(message);
	}

	public static ResourceNotFoundException of(String resource, Object id) {
		return new ResourceNotFoundException("%s not found: %s".formatted(resource, id));
	}
}
