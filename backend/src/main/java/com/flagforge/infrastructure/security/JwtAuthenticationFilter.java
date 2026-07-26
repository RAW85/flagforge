package com.flagforge.infrastructure.security;

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

/**
 * Sets SecurityContext from {@code Authorization: Bearer &lt;jwt&gt;}.
 * Invalid tokens clear the context; protected routes then hit the entry point.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtService jwtService;

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain
	) throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (header == null || !header.startsWith(BEARER_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = header.substring(BEARER_PREFIX.length()).trim();
		if (token.isEmpty()) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			if (SecurityContextHolder.getContext().getAuthentication() == null) {
				AuthenticatedUser principal = jwtService.parseToken(token);
				var authentication = new UsernamePasswordAuthenticationToken(
						principal,
						null,
						principal.getAuthorities()
				);
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (InvalidTokenException ex) {
			SecurityContextHolder.clearContext();
			// Leave unauthenticated; entry point handles protected routes
		}

		filterChain.doFilter(request, response);
	}
}
