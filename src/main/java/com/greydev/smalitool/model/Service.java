package com.greydev.smalitool.model;

import java.util.List;
import java.util.Map;

public class Service {

	// save as file or String?
	private String className;
	private Map<String, List<String>> codeMap;
	private List<String> intentFilterActions;

	public Service() {

	}

	public Service(String className, Map<String, List<String>> codeMap, List<String> intentFilterActions) {
		super();
		this.className = className;
		this.codeMap = codeMap;
		this.intentFilterActions = intentFilterActions;
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

	public List<String> getIntentFilterActions() {
		return intentFilterActions;
	}

	public void setIntentFilterActions(List<String> intentFilterActions) {
		this.intentFilterActions = intentFilterActions;
	}

}
