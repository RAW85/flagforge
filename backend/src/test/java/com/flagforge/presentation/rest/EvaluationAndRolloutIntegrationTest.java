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
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EvaluationAndRolloutIntegrationTest {

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
				"eval-admin-" + suffix,
				"eval-admin-" + suffix + "@flagforge.test",
				"password123"
		);
	}

	@Test
	void evaluateBoolean_and_rolloutSaga_flow() throws Exception {
		String suffix = String.valueOf(System.nanoTime());
		String boolKey = "dark-mode-" + suffix;
		String pctKey = "new-checkout-" + suffix;

		MvcResult boolCreate = mockMvc.perform(post("/api/v1/flags")
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
								""".formatted(boolKey)))
				.andExpect(status().isCreated())
				.andReturn();
		String boolId = objectMapper.readTree(boolCreate.getResponse().getContentAsString())
				.get("id").asText();

		mockMvc.perform(post("/api/v1/evaluate")
						.header("Authorization", ApiTestSupport.bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "flagKey": "%s",
								  "environment": "DEVELOPMENT",
								  "subjectId": "user-1"
								}
								""".formatted(boolKey)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reason", is("FLAG_DISABLED")))
				.andExpect(jsonPath("$.value", is("false")));

		mockMvc.perform(post("/api/v1/flags/{id}/toggle", boolId)
						.header("Authorization", ApiTestSupport.bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"enabled": true}
								"""))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/evaluate/{key}", boolKey)
						.param("environment", "DEVELOPMENT")
						.param("subjectId", "user-1")
						.header("Authorization", ApiTestSupport.bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reason", is("BOOLEAN_ENABLED")))
				.andExpect(jsonPath("$.value", is("true")))
				.andExpect(jsonPath("$.bucket", notNullValue()));

		MvcResult pctCreate = mockMvc.perform(post("/api/v1/flags")
						.header("Authorization", ApiTestSupport.bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "key": "%s",
								  "name": "New Checkout",
								  "flagType": "PERCENTAGE",
								  "environment": "DEVELOPMENT",
								  "defaultValue": "false",
								  "percentage": 0
								}
								""".formatted(pctKey)))
				.andExpect(status().isCreated())
				.andReturn();
		String pctId = objectMapper.readTree(pctCreate.getResponse().getContentAsString())
				.get("id").asText();

		MvcResult sagaStart = mockMvc.perform(post("/api/v1/rollouts")
						.header("Authorization", ApiTestSupport.bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "flagId": "%s",
								  "steps": [0, 10, 100]
								}
								""".formatted(pctId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status", is("RUNNING")))
				.andExpect(jsonPath("$.currentPercentage", is(0)))
				.andReturn();

		JsonNode saga = objectMapper.readTree(sagaStart.getResponse().getContentAsString());
		String sagaId = saga.get("id").asText();

		mockMvc.perform(post("/api/v1/rollouts/{id}/advance", sagaId)
						.header("Authorization", ApiTestSupport.bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currentPercentage", is(10)));

		mockMvc.perform(post("/api/v1/rollouts/{id}/advance", sagaId)
						.header("Authorization", ApiTestSupport.bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currentPercentage", is(100)))
				.andExpect(jsonPath("$.status", is("COMPLETED")));

		mockMvc.perform(post("/api/v1/rollouts/{id}/rollback", sagaId)
						.header("Authorization", ApiTestSupport.bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"reason":"test rollback"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("ROLLED_BACK")));

		mockMvc.perform(get("/api/v1/flags/{id}", pctId)
						.header("Authorization", ApiTestSupport.bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.enabled", is(false)))
				.andExpect(jsonPath("$.percentage", is(0)));
	}
}
