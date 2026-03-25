package com.ohpen.configtracker.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ohpen.configtracker.model.ConfigChange;

public interface ConfigChangeRepository {

	ConfigChange save(ConfigChange configChange);

	Optional<ConfigChange> findById(UUID id);

	List<ConfigChange> findAll();

}
