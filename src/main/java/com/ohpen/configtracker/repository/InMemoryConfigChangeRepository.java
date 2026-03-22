package com.ohpen.configtracker.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.ohpen.configtracker.model.ConfigChange;

@Repository
public class InMemoryConfigChangeRepository implements ConfigChangeRepository {

	private final ConcurrentHashMap<UUID, ConfigChange> storage = new ConcurrentHashMap<>();

	@Override
	public ConfigChange save(ConfigChange configChange) {
		UUID id = configChange.getId();
		storage.put(id, configChange);
		return configChange;
	}

	@Override
	public Optional<ConfigChange> findById(UUID id) {
		return Optional.ofNullable(storage.get(id));
	}

	@Override
	public List<ConfigChange> findAll() {
		return new ArrayList<>(storage.values());
	}

}
