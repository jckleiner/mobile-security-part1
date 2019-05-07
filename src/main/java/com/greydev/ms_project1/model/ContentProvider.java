package com.greydev.ms_project1.model;

public class ContentProvider {

	// save as file or String?
	private String smaliClassPath;
	private String className;

	public ContentProvider() {

	}

	public ContentProvider(String smaliClassPath, String className) {
		super();
		this.smaliClassPath = smaliClassPath;
		this.className = className;
	}

	// no intent-filter
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

}
