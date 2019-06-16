package com.greydev.smalitool.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Slice {
	
	private final String className;
	private final String methodSignature;
	private Set<String> registerSet = new HashSet<>();
	private Map<Integer, String> codeList = new TreeMap<>();

	public Slice(String className, String methodSignature) {
		this.className = className;
		this.methodSignature = methodSignature;
	}
	
	/**
	 * className has no setter, it has to be set via constructor.
	 * @return The name of the class this slice is taken from.
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 * methodSignature has no setter, it has to be set via constructor.
	 * @return The signature of the method this slice is taken from.
	 */
	public String getMethodSignature() {
		return methodSignature;
	}

	public Set<String> getRegisterSet() {
		return registerSet;
	}
	
	public void setRegisterSet(Set<String> registerSet) {
		this.registerSet.clear();
		this.registerSet.addAll(registerSet);
	}
	
	public Map<Integer, String> getCodeList() {
		return codeList;
	}
	
	/**
	 * {@code codeList} is a Treemap.
	 * @param key lineNumber
	 * @param value codeLine
	 */
	public void addToCodeList(Integer key, String value) {
		codeList.put(key, value);
	}
	
	public boolean isValidSlice() {
		return !codeList.isEmpty() && !registerSet.isEmpty();
	}
}
