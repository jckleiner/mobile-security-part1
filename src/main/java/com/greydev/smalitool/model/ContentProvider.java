package com.greydev.smalitool.model;

import java.util.List;
import java.util.Map;

public class ContentProvider {

	// save as file or String?
	private String className;
	private Map<String, List<String>> codeMap;

	public ContentProvider() {

	}

	public ContentProvider(String className, Map<String, List<String>> codeMap) {
		super();
		this.className = className;
		this.codeMap = codeMap;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Map<String, List<String>> getCodeMap() {
		return codeMap;
	}

	public void setCodeMap(Map<String, List<String>> codeMap) {
		this.codeMap = codeMap;
	}

}
