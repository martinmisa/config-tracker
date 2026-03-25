package com.ohpen.configtracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ohpen.configtracker.dto.MetricsResponse;
import com.ohpen.configtracker.service.ConfigChangeService;

@RestController
public class MetricsController {

	private final ConfigChangeService configChangeService;

	public MetricsController(ConfigChangeService configChangeService) {
		this.configChangeService = configChangeService;
	}

	@GetMapping("/metrics")
	public MetricsResponse getMetrics() {
		return configChangeService.getMetrics();
	}

}
