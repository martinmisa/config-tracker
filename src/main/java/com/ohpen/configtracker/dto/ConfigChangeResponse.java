package com.ohpen.configtracker.dto;

import java.time.Instant;
import java.util.UUID;

import com.ohpen.configtracker.model.ChangeType;

public class ConfigChangeResponse {

	private UUID id;
	private Instant changedAt;
	private String ruleName;
	private ChangeType type;
	private String oldValue;
	private String newValue;
	private boolean critical;
	private String changedBy;
	private String reason;

	public ConfigChangeResponse() {
	}

	public ConfigChangeResponse(UUID id, Instant changedAt, String ruleName, ChangeType type, String oldValue,
			String newValue, boolean critical, String changedBy, String reason) {
		this.id = id;
		this.changedAt = changedAt;
		this.ruleName = ruleName;
		this.type = type;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.critical = critical;
		this.changedBy = changedBy;
		this.reason = reason;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Instant getChangedAt() {
		return changedAt;
	}

	public void setChangedAt(Instant changedAt) {
		this.changedAt = changedAt;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public ChangeType getType() {
		return type;
	}

	public void setType(ChangeType type) {
		this.type = type;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public boolean isCritical() {
		return critical;
	}

	public void setCritical(boolean critical) {
		this.critical = critical;
	}

	public String getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(String changedBy) {
		this.changedBy = changedBy;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}
