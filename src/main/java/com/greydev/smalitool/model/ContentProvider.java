package com.greydev.smalitool.model;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.greydev.smalitool.Utils;

public class ContentProvider {

	private static final Logger LOG = Utils.getConfiguredLogger(ContentProvider.class);

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

	public void printInfo() {
		LOG.info("\nClass name: {}", this.getClassName());
		LOG.info("Smali classes:");
		for (String s : this.getCodeMap().keySet()) {
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

}
