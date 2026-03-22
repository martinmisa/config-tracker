package com.ohpen.configtracker.controller;

import org.springframework.web.bind.annotation.*;

import com.ohpen.configtracker.dto.CreateConfigChangeRequest;
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

}
