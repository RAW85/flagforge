package com.flagforge.infrastructure.security;

import com.flagforge.domain.apikey.ApiKey;
import com.flagforge.domain.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

/**
 * Authenticates SDK requests via {@code X-API-Key} or {@code Authorization: ApiKey &lt;key&gt;}.
 */
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

	public static final String API_KEY_HEADER = "X-API-Key";
	private static final String API_KEY_AUTH_PREFIX = "ApiKey ";

	private final ApiKeyRepository apiKeyRepository;
	private final ApiKeyHasher apiKeyHasher;

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain
	) throws ServletException, IOException {
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			extractRawKey(request).ifPresent(rawKey -> authenticate(request, rawKey));
		}
		filterChain.doFilter(request, response);
	}

	private Optional<String> extractRawKey(HttpServletRequest request) {
		String headerKey = request.getHeader(API_KEY_HEADER);
		if (headerKey != null && !headerKey.isBlank()) {
			return Optional.of(headerKey.trim());
		}

		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization != null && authorization.startsWith(API_KEY_AUTH_PREFIX)) {
			return Optional.of(authorization.substring(API_KEY_AUTH_PREFIX.length()).trim());
		}
		return Optional.empty();
	}

	private void authenticate(HttpServletRequest request, String rawKey) {
		if (!apiKeyHasher.looksLikeApiKey(rawKey)) {
			return;
		}
		String hash = apiKeyHasher.hash(rawKey);
		apiKeyRepository.findByKeyHashAndActiveTrue(hash).ifPresent(apiKey -> {
			ApiKeyPrincipal principal = toPrincipal(apiKey);
			var authentication = new UsernamePasswordAuthenticationToken(
					principal,
					null,
					principal.getAuthorities()
			);
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);

			apiKey.setLastUsedAt(Instant.now());
			apiKeyRepository.save(apiKey);
		});
	}

	private ApiKeyPrincipal toPrincipal(ApiKey apiKey) {
		return new ApiKeyPrincipal(
				apiKey.getId(),
				apiKey.getName(),
				apiKey.getKeyPrefix(),
				apiKey.getOwnerId(),
				apiKey.getEnvironmentScope()
		);
	}
}
