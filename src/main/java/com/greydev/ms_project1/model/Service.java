package com.greydev.ms_project1.model;

import java.util.List;

public class Service {

	// save as file or String?
	private String smaliClassPath;
	private String className;
	private List<String> intentFilterActions;

	public Service() {

	}

	public Service(String smaliClassPath, String className, List<String> intentFilterActions) {
		super();
		this.smaliClassPath = smaliClassPath;
		this.className = className;
		this.intentFilterActions = intentFilterActions;
	}

	public String getSmaliClassPath() {
		return smaliClassPath;
	}

	public void setSmaliClassPath(String smaliClassPath) {
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
