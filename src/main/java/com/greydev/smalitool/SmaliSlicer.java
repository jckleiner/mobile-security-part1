package com.greydev.smalitool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Nonnull;

import com.greydev.smalitool.model.Activity;
import com.greydev.smalitool.model.Apk;
import com.greydev.smalitool.model.Slice;
import com.greydev.smalitool.model.SliceContainer;

public class SmaliSlicer {

	private static final String METHODDEFINITIONSTART = ".method";
	private static final String METHODDEFINITIONEND = ".end method";

	/**
	 * This method will create all intra-procedural backward slices for a given APK
	 * and method signature.
	 * 
	 * @param apk             The APK representation that is to be sliced.
	 * @param methodSignature The smali signature of the method for which the APK
	 *                        will be checked.
	 * @return All slices found for the given method signature mapped to the
	 *         activities.
	 */
	@Nonnull
	public SliceContainer createBackwardSlices(@Nonnull Apk apk, @Nonnull String methodSignature) {
		Objects.requireNonNull(apk, "apk must not be null");
		Objects.requireNonNull(methodSignature, "methodSignature must not be null");

		SliceContainer sliceContainer = new SliceContainer(apk.getName());

		for (Map.Entry<String, Activity> clazz : apk.getActivities().entrySet()) {

			sliceContainer.addToSliceMap(clazz.getKey(), createActivitySlices(clazz.getValue(), methodSignature));
		}

		return sliceContainer;
	}

	@Nonnull
	private List<Slice> createActivitySlices(@Nonnull Activity activity, @Nonnull String methodSignature) {
		List<Slice> sliceList = new ArrayList<>();
		List<Slice> validSliceList = new ArrayList<>();

		for (Map.Entry<String, List<String>> activityClass : activity.getCodeMap().entrySet()) {
			sliceList = createClassSlices(activityClass.getKey(), activityClass.getValue(), methodSignature);

		}
		for (Slice slice : sliceList) {
			if (slice.isValidSlice()) {
				sliceList.add(slice);
			}
		}

		return validSliceList;
	}

	@Nonnull
	private List<Slice> createClassSlices(@Nonnull String className, @Nonnull List<String> codeList,
			@Nonnull String methodSignature) {
		List<Slice> sliceList = new ArrayList<>();

		boolean insideMethod = false;

		String currentMethodSignature = "";
		List<Integer> lineNumbersToCheck = new ArrayList<>();
		Map<Integer, String> currentMethodCodeMap = new TreeMap<>(); // Maps line to line number

		for (int lineNumber = 0; lineNumber < codeList.size(); lineNumber++) {
			String codeLine = codeList.get(lineNumber);
			if (codeLine.contains(METHODDEFINITIONSTART)) {
				insideMethod = true;
				currentMethodSignature = codeLine;
				currentMethodCodeMap.clear();
				continue;
			}
			if (codeLine.contains(methodSignature)) {
				lineNumbersToCheck.add(lineNumber);
			}
			if (codeLine.contains(METHODDEFINITIONEND)) {
				insideMethod = false;

				if (!lineNumbersToCheck.isEmpty()) {
					sliceList.addAll(createMethodSlices(className, currentMethodSignature, currentMethodCodeMap,
							lineNumbersToCheck, methodSignature));
					lineNumbersToCheck.clear();
				}
			}
			if (insideMethod) {
				currentMethodCodeMap.put(lineNumber, codeLine);
			}
		}
		return sliceList;
	}

	@Nonnull
	private List<Slice> createMethodSlices(@Nonnull String className, @Nonnull String currentMethodSignature,
			@Nonnull Map<Integer, String> currentMethodCodeMap, @Nonnull List<Integer> lineNumbersToCheck,
			String methodSignature) {

		List<Slice> sliceList = new ArrayList<>();

		for (Integer lineNumber : lineNumbersToCheck) {
			sliceList.add(createBackwardSlice(className, currentMethodSignature, currentMethodCodeMap, lineNumber));
		}

		return sliceList;
	}

	@Nonnull 
	private Slice createBackwardSlice(String className, String currentMethodSignature,
			Map<Integer, String> currentMethodCodeMap, Integer startingLine) {
		Slice slice = new Slice(className, currentMethodSignature);

		int lowestLineNumber = ((TreeMap<Integer, String>) currentMethodCodeMap).firstKey();

		String firstLine = currentMethodCodeMap.get(startingLine);

		slice.addToCodeList(startingLine, firstLine);

		Set<String> registerSet = getInputRegistersFromLine(firstLine);

		for (int lineNumber = startingLine; lineNumber >= lowestLineNumber; lineNumber--) {
			String codeLine = currentMethodCodeMap.get(lineNumber);
			if (lineContainsRegister(codeLine, registerSet)) {
				slice.addToCodeList(lineNumber, codeLine);
				registerSet.addAll(getInputRegistersFromLine(codeLine));
			}
		}

		return slice;
	}

	private boolean lineContainsRegister(String codeLine, Set<String> registerSet) {
		// TODO this should only return true, if the line is relevant, e.g. the register
		// has to be defined and not only used
		for (String register : registerSet) {
			if (codeLine.contains(register)) {
				return true;
			}
		}
		return false;
	}

	private Set<String> getInputRegistersFromLine(String codeLine) {
		Set<String> registerSet = new HashSet<>();
		// TODO actually get the right registers
		return registerSet;
	}
}
