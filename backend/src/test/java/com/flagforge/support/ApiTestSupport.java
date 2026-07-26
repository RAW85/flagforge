package com.flagforge.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flagforge.domain.repository.UserRepository;
import com.flagforge.domain.user.User;
import com.flagforge.domain.user.UserRole;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class ApiTestSupport {

	private ApiTestSupport() {
	}

	public static String registerAndGetToken(
			MockMvc mockMvc,
			ObjectMapper objectMapper,
			String username,
			String email,
			String password
	) throws Exception {
		String body = """
				{
				  "username": "%s",
				  "email": "%s",
				  "password": "%s"
				}
				""".formatted(username, email, password);

		MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andReturn();

		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
		return json.get("accessToken").asText();
	}

	/**
	 * Registers a user and forces ADMIN role so tests stay independent of registration order.
	 * Returns a fresh JWT after promotion.
	 */
	public static String registerAdminAndGetToken(
			MockMvc mockMvc,
			ObjectMapper objectMapper,
			UserRepository userRepository,
			String username,
			String email,
			String password
	) throws Exception {
		registerAndGetToken(mockMvc, objectMapper, username, email, password);

		User user = userRepository.findByEmail(email.toLowerCase())
				.orElseThrow(() -> new IllegalStateException("User not found after register"));
		user.setRole(UserRole.ADMIN);
		userRepository.save(user);

		// Login again so JWT carries ROLE_ADMIN
		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "%s"
								}
								""".formatted(email, password)))
				.andExpect(status().isOk())
				.andReturn();

		return objectMapper.readTree(login.getResponse().getContentAsString())
				.get("accessToken")
				.asText();
	}

	public static String bearer(String token) {
		return "Bearer " + token;
	}
}
