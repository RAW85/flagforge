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
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SdkApiKeyIntegrationTest {

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
				"sdk-admin-" + suffix,
				"sdk-admin-" + suffix + "@flagforge.test",
				"password123"
		);
	}

	@Test
	void createApiKey_and_evaluateViaSdkEndpoint() throws Exception {
		String flagKey = "sdk-flag-" + System.nanoTime();

		// Create + enable a boolean flag
		MvcResult createFlag = mockMvc.perform(post("/api/v1/flags")
						.header("Authorization", ApiTestSupport.bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "key": "%s",
								  "name": "SDK Flag",
								  "flagType": "BOOLEAN",
								  "environment": "DEVELOPMENT",
								  "defaultValue": "false"
								}
								""".formatted(flagKey)))
				.andExpect(status().isCreated())
				.andReturn();
		String flagId = objectMapper.readTree(createFlag.getResponse().getContentAsString())
				.get("id").asText();

		mockMvc.perform(post("/api/v1/flags/{id}/toggle", flagId)
						.header("Authorization", ApiTestSupport.bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"enabled": true}
								"""))
				.andExpect(status().isOk());

		// Create API key
		MvcResult keyResult = mockMvc.perform(post("/api/v1/api-keys")
						.header("Authorization", ApiTestSupport.bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "mobile-sdk",
								  "environmentScope": "DEVELOPMENT"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.rawKey", startsWith("ffk_")))
				.andExpect(jsonPath("$.apiKey.active", is(true)))
				.andReturn();

		JsonNode keyJson = objectMapper.readTree(keyResult.getResponse().getContentAsString());
		String rawKey = keyJson.get("rawKey").asText();
		String keyId = keyJson.get("apiKey").get("id").asText();

		// SDK evaluate with X-API-Key (no JWT)
		mockMvc.perform(post("/api/v1/sdk/evaluate")
						.header("X-API-Key", rawKey)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "flagKey": "%s",
								  "environment": "DEVELOPMENT",
								  "subjectId": "device-1"
								}
								""".formatted(flagKey)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.value", is("true")))
				.andExpect(jsonPath("$.reason", is("BOOLEAN_ENABLED")));

		// Scoped key cannot evaluate other environments
		mockMvc.perform(get("/api/v1/sdk/evaluate/{key}", flagKey)
						.param("environment", "PRODUCTION")
						.param("subjectId", "device-1")
						.header("X-API-Key", rawKey))
				.andExpect(status().isUnprocessableEntity());

		// Without key → 401
		mockMvc.perform(post("/api/v1/sdk/evaluate")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "flagKey": "%s",
								  "environment": "DEVELOPMENT",
								  "subjectId": "device-1"
								}
								""".formatted(flagKey)))
				.andExpect(status().isUnauthorized());

		// Revoke key
		mockMvc.perform(delete("/api/v1/api-keys/{id}", keyId)
						.header("Authorization", ApiTestSupport.bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.active", is(false)));

		// Revoked key rejected
		mockMvc.perform(post("/api/v1/sdk/evaluate")
						.header("X-API-Key", rawKey)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "flagKey": "%s",
								  "environment": "DEVELOPMENT",
								  "subjectId": "device-1"
								}
								""".formatted(flagKey)))
				.andExpect(status().isUnauthorized());
	}
}
