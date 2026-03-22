package com.ohpen.configtracker.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ohpen.configtracker.model.ConfigChange;

@Service
public class LoggingMonitoringNotifier implements MonitoringNotifier {

	private static final Logger log = LoggerFactory.getLogger(LoggingMonitoringNotifier.class);

	@Override
	public void notifyCriticalChange(ConfigChange configChange) {
		log.info("Sending monitoring notification for critical config change id={}", configChange.getId());
	}

}
