package com.greydev.smalitool.model;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.greydev.smalitool.Utils;

public class BroadcastReceiver {

	private static final Logger LOG = Utils.getConfiguredLogger(BroadcastReceiver.class);

	private String className;
	private Map<String, Map<Integer, String>> codeMap; // Map<'smaliFileName.smali', code>
	private List<String> intentFilterActions;

	public BroadcastReceiver() {

	}

	public BroadcastReceiver(String className, Map<String, Map<Integer, String>> codeMap, List<String> intentFilterActions) {
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
		if (this.getIntentFilterActions().isEmpty()) {
			LOG.info("No intent filter actions found.");
		}
		else {
			LOG.info("Intent filter actions:");
			for (String s : this.getIntentFilterActions()) {
				LOG.info(s);
			}
		}
	}

	public void printCodeForSmaliClass(String smaliClassName) {
		LOG.info("\nSmali class: {}", smaliClassName);
		LOG.info("Smali code:");
		for (String s : this.getCodeMap().get(smaliClassName).values()) {
			LOG.info(s);
		}
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Map<String, Map<Integer, String>> getCodeMap() {
		return codeMap;
	}

	public void setCodeMap(Map<String, Map<Integer, String>> codeMap) {
		this.codeMap = codeMap;
	}

	public List<String> getIntentFilterActions() {
		return intentFilterActions;
	}

	public void setIntentFilterActions(List<String> intentFilterActions) {
		this.intentFilterActions = intentFilterActions;
	}

}
