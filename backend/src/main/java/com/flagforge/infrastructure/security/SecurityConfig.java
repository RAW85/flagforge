package com.flagforge.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Dual auth: API key filter (SDK) then JWT filter (dashboard).
 * Order-0 chain permits {@code /h2-console/**} without API filters.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
	private final JsonAuthHandlers jsonAuthHandlers;

	@Bean
	@org.springframework.core.annotation.Order(1)
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.sessionManagement(session ->
						session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(jsonAuthHandlers)
						.accessDeniedHandler(jsonAuthHandlers)
				)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/api/v1/auth/register",
								"/api/v1/auth/login",
								"/swagger-ui/**",
								"/swagger-ui.html",
								"/v3/api-docs/**",
								"/actuator/health"
						).permitAll()
						// Public SDK evaluation — API key principal (ROLE_SDK)
						.requestMatchers("/api/v1/sdk/**")
						.hasRole("SDK")
						// Dashboard evaluation — JWT roles
						.requestMatchers("/api/v1/evaluate", "/api/v1/evaluate/**")
						.hasAnyRole("VIEWER", "EDITOR", "ADMIN")
						// API key management
						.requestMatchers(HttpMethod.GET, "/api/v1/api-keys", "/api/v1/api-keys/**")
						.hasAnyRole("EDITOR", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/v1/api-keys", "/api/v1/api-keys/**")
						.hasAnyRole("EDITOR", "ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/v1/api-keys/**")
						.hasAnyRole("EDITOR", "ADMIN")
						// Admin user management
						.requestMatchers("/api/v1/users", "/api/v1/users/**")
						.hasRole("ADMIN")
						// Read flags / rollouts
						.requestMatchers(HttpMethod.GET, "/api/v1/flags/**")
						.hasAnyRole("VIEWER", "EDITOR", "ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/v1/rollouts", "/api/v1/rollouts/**")
						.hasAnyRole("VIEWER", "EDITOR", "ADMIN")
						// Mutations
						.requestMatchers(HttpMethod.POST, "/api/v1/flags/**")
						.hasAnyRole("EDITOR", "ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/v1/flags/**")
						.hasAnyRole("EDITOR", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/v1/rollouts", "/api/v1/rollouts/**")
						.hasAnyRole("EDITOR", "ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/v1/flags/**")
						.hasRole("ADMIN")
						.anyRequest().authenticated()
				)
				// H2 console uses frames; same-origin is required for the console UI
				.headers(headers -> headers
						.frameOptions(frame -> frame.sameOrigin())
				)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				// API key first (for SDK), then JWT (for dashboard)
				.addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * Separate chain so H2 console is not blocked by JWT/API-key filters or strict API error handling paths.
	 */
	@Bean
	@org.springframework.core.annotation.Order(0)
	SecurityFilterChain h2ConsoleSecurityFilterChain(HttpSecurity http) throws Exception {
		http
				.securityMatcher("/h2-console/**")
				.csrf(AbstractHttpConfigurer::disable)
				.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of(
				"http://localhost:5173",
				"http://127.0.0.1:5173"
		));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
