package com.ohpen.configtracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ohpen.configtracker.dto.CreateConfigChangeRequest;
import com.ohpen.configtracker.exception.ConfigChangeNotFoundException;
import com.ohpen.configtracker.exception.InvalidConfigChangeException;
import com.ohpen.configtracker.integration.MonitoringNotifier;
import com.ohpen.configtracker.model.ChangeType;
import com.ohpen.configtracker.model.ConfigChange;
import com.ohpen.configtracker.repository.ConfigChangeRepository;

@ExtendWith(MockitoExtension.class)
class ConfigChangeServiceTest {

	@Mock
	private ConfigChangeRepository configChangeRepository;

	@Mock
	private MonitoringNotifier monitoringNotifier;

	@InjectMocks
	private ConfigChangeService configChangeService;

	@BeforeEach
	void setUp() {
		lenient().when(configChangeRepository.save(any(ConfigChange.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	void shouldCreateAddConfigChangeWhenRequestIsValid() {
		CreateConfigChangeRequest request = new CreateConfigChangeRequest(
				"credit-limit", ChangeType.ADD, null, "500", false, "alice", "onboarding");

		ConfigChange result = configChangeService.createConfigChange(request);

		assertThat(result.getId()).isNotNull();
		assertThat(result.getRuleName()).isEqualTo("credit-limit");
		assertThat(result.getType()).isEqualTo(ChangeType.ADD);
		assertThat(result.getNewValue()).isEqualTo("500");
		assertThat(result.isCritical()).isFalse();
		verify(configChangeRepository).save(any(ConfigChange.class));
		verify(monitoringNotifier, never()).notifyCriticalChange(any());
	}

	@Test
	void shouldNotifyMonitoringWhenCriticalChangeIsCreated() {
		CreateConfigChangeRequest request = new CreateConfigChangeRequest(
				"rule", ChangeType.ADD, null, "v", true, "bob", "reason");

		configChangeService.createConfigChange(request);

		verify(monitoringNotifier).notifyCriticalChange(argThat(ConfigChange::isCritical));
	}

	@Test
	void shouldNotNotifyMonitoringWhenChangeIsNotCritical() {
		CreateConfigChangeRequest request = new CreateConfigChangeRequest(
				"rule", ChangeType.ADD, null, "v", false, "bob", "reason");

		configChangeService.createConfigChange(request);

		verify(monitoringNotifier, never()).notifyCriticalChange(any());
	}

	@Test
	void shouldThrowInvalidConfigChangeExceptionWhenAddHasBlankNewValue() {
		CreateConfigChangeRequest request = new CreateConfigChangeRequest(
				"rule", ChangeType.ADD, null, "   ", false, "u", "r");

		assertThatThrownBy(() -> configChangeService.createConfigChange(request))
				.isInstanceOf(InvalidConfigChangeException.class)
				.hasMessageContaining("ADD");

		verify(configChangeRepository, never()).save(any());
	}

	@Test
	void shouldThrowInvalidConfigChangeExceptionWhenUpdateHasSameOldAndNewValue() {
		CreateConfigChangeRequest request = new CreateConfigChangeRequest(
				"rule", ChangeType.UPDATE, "same", "same", false, "u", "r");

		assertThatThrownBy(() -> configChangeService.createConfigChange(request))
				.isInstanceOf(InvalidConfigChangeException.class)
				.hasMessageContaining("UPDATE");

		verify(configChangeRepository, never()).save(any());
	}

	@Test
	void shouldReturnConfigChangeByIdWhenItExists() {
		UUID id = UUID.randomUUID();
		ConfigChange existing = new ConfigChange(id, Instant.now(), "rule", ChangeType.ADD, null, "x", false, "u", "r");
		when(configChangeRepository.findById(id)).thenReturn(Optional.of(existing));

		ConfigChange result = configChangeService.getConfigChangeById(id);

		assertThat(result).isSameAs(existing);
	}

	@Test
	void shouldThrowConfigChangeNotFoundExceptionWhenIdDoesNotExist() {
		UUID id = UUID.randomUUID();
		when(configChangeRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> configChangeService.getConfigChangeById(id))
				.isInstanceOf(ConfigChangeNotFoundException.class)
				.hasMessageContaining(id.toString());
	}

}
