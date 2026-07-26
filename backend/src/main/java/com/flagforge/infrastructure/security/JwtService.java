package com.flagforge.infrastructure.security;

import com.flagforge.domain.user.User;
import com.flagforge.domain.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/** Issues and parses HS256 JWTs (subject = user id; claims: email, username, role). */
@Service
public class JwtService {

	private final JwtProperties properties;
	private final SecretKey secretKey;

	public JwtService(JwtProperties properties) {
		this.properties = properties;
		byte[] keyBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
		if (keyBytes.length < 32) {
			throw new IllegalStateException(
					"flagforge.security.jwt.secret must be at least 32 bytes for HS256"
			);
		}
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateToken(User user) {
		Instant now = Instant.now();
		Instant expiry = now.plusMillis(properties.expirationMs());

		return Jwts.builder()
				.subject(user.getId().toString())
				.claim("email", user.getEmail())
				.claim("username", user.getUsername())
				.claim("role", user.getRole().name())
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiry))
				.signWith(secretKey)
				.compact();
	}

	public AuthenticatedUser parseToken(String token) {
		try {
			Claims claims = Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();

			UUID userId = UUID.fromString(claims.getSubject());
			String email = claims.get("email", String.class);
			String username = claims.get("username", String.class);
			UserRole role = UserRole.valueOf(claims.get("role", String.class));

			return new AuthenticatedUser(userId, username, email, role, true);
		} catch (JwtException | IllegalArgumentException ex) {
			throw new InvalidTokenException("Invalid or expired JWT", ex);
		}
	}

	public long getExpirationMs() {
		return properties.expirationMs();
	}
}
