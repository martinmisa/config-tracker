package com.ohpen.configtracker.integration;

import com.ohpen.configtracker.model.ConfigChange;

public interface MonitoringNotifier {

	void notifyCriticalChange(ConfigChange configChange);

}
