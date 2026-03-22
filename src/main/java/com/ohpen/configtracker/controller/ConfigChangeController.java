package com.ohpen.configtracker.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.ohpen.configtracker.dto.CreateConfigChangeRequest;
import com.ohpen.configtracker.model.ChangeType;
import com.ohpen.configtracker.model.ConfigChange;
import com.ohpen.configtracker.service.ConfigChangeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/config-changes")
public class ConfigChangeController {

	private final ConfigChangeService configChangeService;

	public ConfigChangeController(ConfigChangeService configChangeService) {
		this.configChangeService = configChangeService;
	}

	@PostMapping
	public ConfigChange createConfigChange(@Valid @RequestBody CreateConfigChangeRequest request) {
		return configChangeService.createConfigChange(request);
	}

	@GetMapping
	public List<ConfigChange> getConfigChanges(
			@RequestParam(required = false) ChangeType type,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
		return configChangeService.getConfigChanges(type, from, to);
	}

	@GetMapping("/{id}")
	public ConfigChange getConfigChangeById(@PathVariable UUID id) {
		return configChangeService.getConfigChangeById(id);
	}

}
