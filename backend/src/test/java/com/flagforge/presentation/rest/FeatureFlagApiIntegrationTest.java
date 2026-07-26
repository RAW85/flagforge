package com.flagforge.presentation.rest;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FeatureFlagApiIntegrationTest {

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
				"flag-admin-" + suffix,
				"flag-admin-" + suffix + "@flagforge.test",
				"password123"
		);
	}

	@Test
	void create_list_toggle_and_delete_flag() throws Exception {
		String key = "dark-mode-" + System.nanoTime();
		MvcResult createResult = mockMvc.perform(post("/api/v1/flags")
						.header("Authorization", ApiTestSupport.bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "key": "%s",
								  "name": "Dark Mode",
								  "flagType": "BOOLEAN",
								  "environment": "DEVELOPMENT",
								  "defaultValue": "false"
								}
								""".formatted(key)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.key", is(key)))
				.andExpect(jsonPath("$.enabled", is(false)))
				.andExpect(jsonPath("$.status", is("DRAFT")))
				.andReturn();

		JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
		String id = created.get("id").asText();

		mockMvc.perform(get("/api/v1/flags")
						.header("Authorization", ApiTestSupport.bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items").isArray());

		mockMvc.perform(post("/api/v1/flags/{id}/toggle", id)
						.header("Authorization", ApiTestSupport.bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"enabled": true}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.enabled", is(true)))
				.andExpect(jsonPath("$.status", is("ACTIVE")));

		mockMvc.perform(delete("/api/v1/flags/{id}", id)
						.header("Authorization", ApiTestSupport.bearer(adminToken)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/flags/{id}", id)
						.header("Authorization", ApiTestSupport.bearer(adminToken)))
				.andExpect(status().isNotFound());
	}

	@Test
	void unauthenticated_access_isRejected() throws Exception {
		mockMvc.perform(get("/api/v1/flags"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void viewer_cannot_create_flag() throws Exception {
		String suffix = String.valueOf(System.nanoTime());
		// Seed so this registration is not the first user
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

		mockMvc.perform(post("/api/v1/flags")
						.header("Authorization", ApiTestSupport.bearer(viewerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "key": "blocked-flag-%s",
								  "name": "Blocked",
								  "flagType": "BOOLEAN",
								  "environment": "DEVELOPMENT"
								}
								""".formatted(suffix)))
				.andExpect(status().isForbidden());
	}
}
