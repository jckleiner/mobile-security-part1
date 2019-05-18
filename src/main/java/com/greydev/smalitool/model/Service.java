package com.greydev.smalitool.model;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.greydev.smalitool.Utils;

public class Service {

	private static final Logger LOG = Utils.getConfiguredLogger(Service.class);

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

	public void printInfo() {
		LOG.info("\nClass name: {}", this.getClassName());
		LOG.info("Smali classes:");
		for (String s : this.getCodeMap().keySet()) {
			LOG.info(s);
		}
		LOG.info("Intent filter actions:");
		for (String s : this.getIntentFilterActions()) {
			LOG.info(s);
		}
	}

	public void printCodeForSmaliClass(String pathToSmaliClass) {
		LOG.info("\nSmali class: {}", pathToSmaliClass);
		LOG.info("Smali code:");
		for (String s : this.getCodeMap().get(pathToSmaliClass)) {
			LOG.info(s);
		}
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
