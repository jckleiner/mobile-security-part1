package com.greydev.smalitool.model;

import java.util.HashMap;
import java.util.Map;

public class CodeBlock {

	private String apkName;
	private String className;
	private String methodDefinition;
	private Map<Integer, String> codeLines = new HashMap<>();
	private Map<Integer, String> slicedLines = new HashMap<>();

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

	public Map<Integer, String> getSlicedLines() {
		return slicedLines;
	}

	public void setSlicedLines(Map<Integer, String> slicedLines) {
		this.slicedLines = slicedLines;
	}

	public String getMethodDefinition() {
		return methodDefinition;
	}

	public void setMethodDefinition(String methodDefinition) {
		this.methodDefinition = methodDefinition;
	}

}
