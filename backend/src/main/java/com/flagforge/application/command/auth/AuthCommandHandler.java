package com.flagforge.application.command.auth;

import com.flagforge.domain.audit.AuditAction;
import com.flagforge.domain.audit.AuditEvent;
import com.flagforge.domain.exception.ConflictException;
import com.flagforge.domain.exception.UnauthorizedException;
import com.flagforge.domain.repository.AuditEventRepository;
import com.flagforge.domain.repository.UserRepository;
import com.flagforge.domain.user.User;
import com.flagforge.domain.user.UserRole;
import com.flagforge.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Register / login. First registered user is promoted to ADMIN for local bootstrap.
 */
@Service
@RequiredArgsConstructor
public class AuthCommandHandler {

	private final UserRepository userRepository;
	private final AuditEventRepository auditEventRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	@Transactional
	public AuthResult handle(RegisterUserCommand command) {
		String email = normalizeEmail(command.email());
		String username = command.username().trim();

		if (userRepository.existsByEmail(email)) {
			throw new ConflictException("Email already registered: " + email);
		}
		if (userRepository.existsByUsername(username)) {
			throw new ConflictException("Username already taken: " + username);
		}

		// First registered user becomes ADMIN for local bootstrap convenience.
		UserRole role = userRepository.count() == 0 ? UserRole.ADMIN : UserRole.VIEWER;

		User user = User.builder()
				.username(username)
				.email(email)
				.passwordHash(passwordEncoder.encode(command.password()))
				.role(role)
				.enabled(true)
				.build();

		User saved = userRepository.save(user);
		auditEventRepository.save(AuditEvent.builder()
				.entityType("User")
				.entityId(saved.getId())
				.action(AuditAction.CREATE)
				.actorId(saved.getId())
				.actorEmail(saved.getEmail())
				.detailsJson("{\"event\":\"REGISTER\",\"role\":\"" + saved.getRole() + "\"}")
				.build());

		String token = jwtService.generateToken(saved);
		return AuthResult.of(saved, token, jwtService.getExpirationMs());
	}

	@Transactional
	public AuthResult handle(LoginCommand command) {
		String email = normalizeEmail(command.email());

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

		if (!user.isEnabled()) {
			throw new UnauthorizedException("Account is disabled");
		}
		if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
			throw new UnauthorizedException("Invalid email or password");
		}

		auditEventRepository.save(AuditEvent.builder()
				.entityType("User")
				.entityId(user.getId())
				.action(AuditAction.LOGIN)
				.actorId(user.getId())
				.actorEmail(user.getEmail())
				.build());

		String token = jwtService.generateToken(user);
		return AuthResult.of(user, token, jwtService.getExpirationMs());
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}
}
