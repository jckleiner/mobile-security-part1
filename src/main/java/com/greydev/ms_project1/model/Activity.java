package com.greydev.ms_project1.model;

import java.util.List;

public class Activity {

	// save as file or String?
	private String className;
	private String smaliClassPath;
	private List<String> intentFilterActions;

	// add intent filter category?

	public Activity() {

	}

	public Activity(String className, String smaliClassPath, List<String> intentFilterActions) {
		super();
		this.className = className;
		this.smaliClassPath = smaliClassPath;
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
