package com.flagforge.presentation.rest;

import com.flagforge.application.command.auth.AuthCommandHandler;
import com.flagforge.application.command.auth.LoginCommand;
import com.flagforge.application.command.auth.RegisterUserCommand;
import com.flagforge.infrastructure.security.AuthenticatedUser;
import com.flagforge.infrastructure.security.SecurityUtils;
import com.flagforge.presentation.dto.auth.AuthResponse;
import com.flagforge.presentation.dto.auth.LoginRequest;
import com.flagforge.presentation.dto.auth.MeResponse;
import com.flagforge.presentation.dto.auth.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public register/login plus authenticated {@code /me}. */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registration, login, and current user")
public class AuthController {

	private final AuthCommandHandler authCommandHandler;

	@PostMapping("/register")
	@Operation(summary = "Register a new user (first user becomes ADMIN)")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		var result = authCommandHandler.handle(new RegisterUserCommand(
				request.username(),
				request.email(),
				request.password()
		));
		return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(result));
	}

	@PostMapping("/login")
	@Operation(summary = "Login and receive a JWT")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		var result = authCommandHandler.handle(new LoginCommand(
				request.email(),
				request.password()
		));
		return AuthResponse.from(result);
	}

	@GetMapping("/me")
	@Operation(summary = "Get the authenticated user", security = @SecurityRequirement(name = "bearerAuth"))
	public MeResponse me() {
		AuthenticatedUser user = SecurityUtils.requireCurrentUser();
		return MeResponse.from(user);
	}
}
