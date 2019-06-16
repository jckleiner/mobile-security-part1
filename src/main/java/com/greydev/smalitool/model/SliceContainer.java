package com.greydev.smalitool.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SliceContainer {
	
	private final String apkName;
	/**
	 * Lists of slices mapped to their activities.
	 */
	private Map<String, List<Slice>> sliceMap = new HashMap<>();
	private List<Slice> backwardSlices = new ArrayList<>();
	
	public SliceContainer(String apkName) {
		this.apkName = apkName;
	}

	public String getApkName() {
		return apkName;
	}

	public List<Slice> getBackwardSlices() {
		return backwardSlices;
	}

	public void setBackwardSlices(List<Slice> backwardSlices) {
		this.backwardSlices.clear();
		this.backwardSlices.addAll(backwardSlices);
	}

	public Map<String, List<Slice>> getSliceMap() {
		return sliceMap;
	}

	public void addToSliceMap(String activityName, List<Slice> sliceList) {
		sliceMap.put(activityName, sliceList);
	}

}
