package com.greydev.ms_project1.model;

import java.util.List;

public class ContentProvider {

	// save as file or String?
	private String className;
	private List<String> smaliClassPath;

	public ContentProvider() {

	}

	public ContentProvider(String className, List<String> smaliClassPath) {
		super();
		this.className = className;
		this.smaliClassPath = smaliClassPath;
	}

	// no intent-filter
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

}
