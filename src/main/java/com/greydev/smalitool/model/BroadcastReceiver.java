package com.greydev.smalitool.model;

import java.util.List;

public class BroadcastReceiver {

	// save as file or String?
	private String className;
	private List<String> smaliClassPath;
	private List<String> intentFilterActions;

	public BroadcastReceiver() {

	}

	public BroadcastReceiver(String className, List<String> smaliClassPath, List<String> intentFilterActions) {
		super();
		this.className = className;
		this.smaliClassPath = smaliClassPath;
		this.intentFilterActions = intentFilterActions;
	}

	public List<String> getSmaliClassPath() {
		return smaliClassPath;
	}

	public void setSmaliClassPath(List<String> smaliClassPath) {
		this.smaliClassPath = smaliClassPath;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public List<String> getIntentFilterActions() {
		return intentFilterActions;
	}

	public void setIntentFilterActions(List<String> intentFilterActions) {
		this.intentFilterActions = intentFilterActions;
	}

}