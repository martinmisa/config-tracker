package com.ohpen.configtracker.dto;

import java.util.Map;

public class MetricsResponse {

	private int totalChanges;
	private int criticalChanges;
	private Map<String, Long> changesByType;

	public MetricsResponse() {
	}

	public MetricsResponse(int totalChanges, int criticalChanges, Map<String, Long> changesByType) {
		this.totalChanges = totalChanges;
		this.criticalChanges = criticalChanges;
		this.changesByType = changesByType;
	}

	public int getTotalChanges() {
		return totalChanges;
	}

	public void setTotalChanges(int totalChanges) {
		this.totalChanges = totalChanges;
	}

	public int getCriticalChanges() {
		return criticalChanges;
	}

	public void setCriticalChanges(int criticalChanges) {
		this.criticalChanges = criticalChanges;
	}

	public Map<String, Long> getChangesByType() {
		return changesByType;
	}

	public void setChangesByType(Map<String, Long> changesByType) {
		this.changesByType = changesByType;
	}

}
