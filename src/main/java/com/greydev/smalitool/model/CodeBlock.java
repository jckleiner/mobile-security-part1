package com.greydev.smalitool.model;

import java.util.HashMap;
import java.util.Map;

public class CodeBlock {

	private String apkName;
	private String className;
	private Map<Integer, String> codeLines = new HashMap<>();

	public String getApkName() {
		return apkName;
	}

	public void setApkName(String apkName) {
		this.apkName = apkName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Map<Integer, String> getCodeLines() {
		return codeLines;
	}

	public void setCodeLines(Map<Integer, String> codeLines) {
		this.codeLines = codeLines;
	}

}
