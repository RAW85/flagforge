package com.flagforge.presentation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flagforge.domain.repository.UserRepository;
import com.flagforge.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Test
	void register_login_and_me_flow() throws Exception {
		String suffix = String.valueOf(System.nanoTime());
		String email = "admin-" + suffix + "@flagforge.test";
		String token = ApiTestSupport.registerAdminAndGetToken(
				mockMvc,
				objectMapper,
				userRepository,
				"admin-" + suffix,
				email,
				"password123"
		);

		mockMvc.perform(get("/api/v1/auth/me")
						.header("Authorization", ApiTestSupport.bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email", is(email)))
				.andExpect(jsonPath("$.role", is("ADMIN")));

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "password123"
								}
								""".formatted(email)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken", notNullValue()))
				.andExpect(jsonPath("$.role", is("ADMIN")));
	}

	@Test
	void secondUser_isViewer_whenUsersAlreadyExist() throws Exception {
		String suffix = String.valueOf(System.nanoTime());
		// Ensure at least one user exists
		ApiTestSupport.registerAndGetToken(
				mockMvc,
				objectMapper,
				"seed-" + suffix,
				"seed-" + suffix + "@flagforge.test",
				"password123"
		);

		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "username": "viewer-%s",
								  "email": "viewer-%s@flagforge.test",
								  "password": "password123"
								}
								""".formatted(suffix, suffix)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.role", is("VIEWER")));
	}

	@Test
	void login_withBadPassword_returns401() throws Exception {
		String suffix = String.valueOf(System.nanoTime());
		String email = "user-" + suffix + "@flagforge.test";
		ApiTestSupport.registerAndGetToken(
				mockMvc,
				objectMapper,
				"user-" + suffix,
				email,
				"password123"
		);

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "wrong-password"
								}
								""".formatted(email)))
				.andExpect(status().isUnauthorized());
	}
}
