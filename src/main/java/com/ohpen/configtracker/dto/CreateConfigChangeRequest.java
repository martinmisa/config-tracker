package com.ohpen.configtracker.dto;

import com.ohpen.configtracker.model.ChangeType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateConfigChangeRequest {

	@NotBlank
	private String ruleName;

	@NotNull
	private ChangeType type;

	private String oldValue;

	private String newValue;

	private boolean critical;

	@NotBlank
	private String changedBy;

	@NotBlank
	private String reason;

	public CreateConfigChangeRequest() {
	}

	public CreateConfigChangeRequest(String ruleName, ChangeType type, String oldValue, String newValue,
			boolean critical, String changedBy, String reason) {
		this.ruleName = ruleName;
		this.type = type;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.critical = critical;
		this.changedBy = changedBy;
		this.reason = reason;
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
