package com.ohpen.configtracker.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.ohpen.configtracker.model.ConfigChange;

@Service
public class LoggingMonitoringNotifier implements MonitoringNotifier {

	private static final Logger log = LoggerFactory.getLogger(LoggingMonitoringNotifier.class);

	@Override
	@Retryable(retryFor = RuntimeException.class, maxAttempts = 3, backoff = @Backoff(delay = 200))
	public void notifyCriticalChange(ConfigChange configChange) {
		log.info("Sending monitoring notification for critical config change id={}", configChange.getId());
		if (configChange.getReason() != null && configChange.getReason().toLowerCase().contains("fail")) {
			throw new RuntimeException("Simulated monitoring outage");
		}
	}

	@Recover
	void recover(RuntimeException ex, ConfigChange configChange) {
		log.warn("Monitoring notification retries exhausted for critical config change id={}", configChange.getId(), ex);
	}

}
