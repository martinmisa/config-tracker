package com.ohpen.configtracker.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class ConfigChangeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldCreateConfigChangeSuccessfully() throws Exception {
		mockMvc.perform(post("/api/config-changes")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "ruleName": "limit-a",
						  "type": "ADD",
						  "newValue": "100",
						  "critical": false,
						  "changedBy": "tester",
						  "reason": "integration"
						}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.ruleName").value("limit-a"))
				.andExpect(jsonPath("$.type").value("ADD"))
				.andExpect(jsonPath("$.newValue").value("100"));
	}

	@Test
	void shouldReturnBadRequestWhenValidationFails() throws Exception {
		mockMvc.perform(post("/api/config-changes")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "ruleName": "",
						  "type": "ADD",
						  "newValue": "100",
						  "critical": false,
						  "changedBy": "tester",
						  "reason": "integration"
						}
						"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturnAllConfigChanges() throws Exception {
		String ruleName = "list-" + UUID.randomUUID();
		mockMvc.perform(post("/api/config-changes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(String.format("""
						{
						  "ruleName": "%s",
						  "type": "ADD",
						  "newValue": "1",
						  "critical": false,
						  "changedBy": "u",
						  "reason": "r"
						}
						""", ruleName)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/config-changes"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].ruleName", hasItem(ruleName)));
	}

	@Test
	void shouldReturnConfigChangeById() throws Exception {
		MvcResult created = mockMvc.perform(post("/api/config-changes")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "ruleName": "by-id",
						  "type": "ADD",
						  "newValue": "x",
						  "critical": false,
						  "changedBy": "u",
						  "reason": "r"
						}
						"""))
				.andExpect(status().isCreated())
				.andReturn();

		String id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

		mockMvc.perform(get("/api/config-changes/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.ruleName").value("by-id"));
	}

	@Test
	void shouldReturnNotFoundWhenConfigChangeDoesNotExist() throws Exception {
		UUID missingId = UUID.randomUUID();

		mockMvc.perform(get("/api/config-changes/" + missingId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").exists())
				.andExpect(jsonPath("$.timestamp").exists());
	}

}
