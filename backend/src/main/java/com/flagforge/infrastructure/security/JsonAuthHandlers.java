package com.flagforge.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flagforge.presentation.dto.common.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonAuthHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	public JsonAuthHandlers() {
		this.objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException
	) throws IOException {
		write(response, HttpStatus.UNAUTHORIZED, "Authentication required", request.getRequestURI());
	}

	@Override
	public void handle(
			HttpServletRequest request,
			HttpServletResponse response,
			AccessDeniedException accessDeniedException
	) throws IOException {
		write(response, HttpStatus.FORBIDDEN, "Access denied", request.getRequestURI());
	}

	private void write(HttpServletResponse response, HttpStatus status, String message, String path)
			throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		ApiErrorResponse body = ApiErrorResponse.of(
				status.value(),
				status.getReasonPhrase(),
				message,
				path
		);
		objectMapper.writeValue(response.getOutputStream(), body);
	}
}
