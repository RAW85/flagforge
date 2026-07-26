package com.flagforge.presentation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flagforge.domain.repository.UserRepository;
import com.flagforge.support.ApiTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserAdminIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	private String adminToken;

	@BeforeEach
	void setUp() throws Exception {
		String suffix = String.valueOf(System.nanoTime());
		adminToken = ApiTestSupport.registerAdminAndGetToken(
				mockMvc,
				objectMapper,
				userRepository,
				"ua-admin-" + suffix,
				"ua-admin-" + suffix + "@flagforge.test",
				"password123"
		);
	}

	@Test
	void admin_can_list_and_change_roles() throws Exception {
		String suffix = String.valueOf(System.nanoTime());
		// Create a viewer
		ApiTestSupport.registerAndGetToken(
				mockMvc,
				objectMapper,
				"ua-viewer-" + suffix,
				"ua-viewer-" + suffix + "@flagforge.test",
				"password123"
		);

		var users = userRepository.findAllByOrderByCreatedAtDesc();
		var viewer = users.stream()
				.filter(u -> u.getEmail().startsWith("ua-viewer-"))
				.findFirst()
				.orElseThrow();

		mockMvc.perform(get("/api/v1/users")
						.header("Authorization", ApiTestSupport.bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());

		mockMvc.perform(put("/api/v1/users/{id}/role", viewer.getId())
						.header("Authorization", ApiTestSupport.bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"role":"EDITOR"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role", is("EDITOR")));

		mockMvc.perform(put("/api/v1/users/{id}/enabled", viewer.getId())
						.header("Authorization", ApiTestSupport.bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"enabled":false}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.enabled", is(false)));
	}

	@Test
	void nonAdmin_cannot_list_users() throws Exception {
		String suffix = String.valueOf(System.nanoTime());
		// Ensure not first user
		ApiTestSupport.registerAndGetToken(
				mockMvc,
				objectMapper,
				"seed-" + suffix,
				"seed-" + suffix + "@flagforge.test",
				"password123"
		);
		String viewerToken = ApiTestSupport.registerAndGetToken(
				mockMvc,
				objectMapper,
				"viewer-" + suffix,
				"viewer-" + suffix + "@flagforge.test",
				"password123"
		);

		mockMvc.perform(get("/api/v1/users")
						.header("Authorization", ApiTestSupport.bearer(viewerToken)))
				.andExpect(status().isForbidden());
	}
}
