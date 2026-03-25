package com.ohpen.configtracker.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ohpen.configtracker.dto.CreateConfigChangeRequest;
import com.ohpen.configtracker.dto.MetricsResponse;
import com.ohpen.configtracker.exception.ConfigChangeNotFoundException;
import com.ohpen.configtracker.exception.InvalidConfigChangeException;
import com.ohpen.configtracker.integration.MonitoringNotifier;
import com.ohpen.configtracker.model.ChangeType;
import com.ohpen.configtracker.model.ConfigChange;
import com.ohpen.configtracker.repository.ConfigChangeRepository;

@Service
public class ConfigChangeService {

	private final ConfigChangeRepository configChangeRepository;
	private final MonitoringNotifier monitoringNotifier;

	public ConfigChangeService(ConfigChangeRepository configChangeRepository,
			MonitoringNotifier monitoringNotifier) {
		this.configChangeRepository = configChangeRepository;
		this.monitoringNotifier = monitoringNotifier;
	}

	public ConfigChange createConfigChange(CreateConfigChangeRequest request) {
		validateCreateRequest(request);

		ConfigChange configChange = new ConfigChange(
				UUID.randomUUID(),
				Instant.now(),
				request.getRuleName(),
				request.getType(),
				request.getOldValue(),
				request.getNewValue(),
				request.isCritical(),
				request.getChangedBy(),
				request.getReason());

		ConfigChange saved = configChangeRepository.save(configChange);
		if (saved.isCritical()) {
			monitoringNotifier.notifyCriticalChange(saved);
		}
		return saved;
	}

	public List<ConfigChange> getConfigChanges(ChangeType type, Instant from, Instant to) {
		if (from != null && to != null && from.isAfter(to)) {
			throw new InvalidConfigChangeException("from must not be after to");
		}
		return configChangeRepository.findAll().stream()
				.filter(c -> type == null || type.equals(c.getType()))
				.filter(c -> from == null || !c.getChangedAt().isBefore(from))
				.filter(c -> to == null || !c.getChangedAt().isAfter(to))
				.toList();
	}

	public ConfigChange getConfigChangeById(UUID id) {
		return configChangeRepository.findById(id)
				.orElseThrow(() -> new ConfigChangeNotFoundException("Config change not found for id: " + id));
	}

	public MetricsResponse getMetrics() {
		List<ConfigChange> all = configChangeRepository.findAll();
		int totalChanges = all.size();
		int criticalChanges = (int) all.stream().filter(ConfigChange::isCritical).count();
		Map<String, Long> changesByType = all.stream()
				.collect(Collectors.groupingBy(c -> c.getType().name(), Collectors.counting()));
		return new MetricsResponse(totalChanges, criticalChanges, changesByType);
	}

	private void validateCreateRequest(CreateConfigChangeRequest request) {
		ChangeType type = request.getType();
		if (type == null) {
			throw new InvalidConfigChangeException("Change type must not be null");
		}
		switch (type) {
			case ADD -> {
				if (!isBlank(request.getOldValue())) {
					throw new InvalidConfigChangeException("For ADD, oldValue must be null or blank");
				}
				if (isBlank(request.getNewValue())) {
					throw new InvalidConfigChangeException("For ADD, newValue must not be null or blank");
				}
			}
			case DELETE -> {
				if (!isBlank(request.getNewValue())) {
					throw new InvalidConfigChangeException("For DELETE, newValue must be null or blank");
				}
				if (isBlank(request.getOldValue())) {
					throw new InvalidConfigChangeException("For DELETE, oldValue must not be null or blank");
				}
			}
			case UPDATE -> {
				if (isBlank(request.getOldValue())) {
					throw new InvalidConfigChangeException("For UPDATE, oldValue must not be null or blank");
				}
				if (isBlank(request.getNewValue())) {
					throw new InvalidConfigChangeException("For UPDATE, newValue must not be null or blank");
				}
				if (request.getOldValue().equals(request.getNewValue())) {
					throw new InvalidConfigChangeException("For UPDATE, oldValue and newValue must not be equal");
				}
			}
		}
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

}
