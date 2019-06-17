package com.greydev.smalitool;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.greydev.smalitool.model.Apk;
import com.greydev.smalitool.model.CodeBlock;

public class Slicer {

	private static final String METHOD_START = ".method";
	private static final String OUTPUT_FILE_NAME = System.getProperty("user.home") + "\\smalitool_slices.txt";
	private static final String BLOCK_SEPARATOR = "\n\n######################################################"
			+ "######################################\n";

	public void startSlicing(Map<String, Apk> apkList, String sliceMethodPrototype) {
		List<CodeBlock> blocksToSlice = new ArrayList<>();

		System.out.println("\n\n################# Starting Slicing #################\n");
		System.out.println("Searching for: " + sliceMethodPrototype);

		// search every class for the given method signature
		apkList.values().forEach(apk -> {
			apk.getActivities().values().forEach(activity -> {
				activity.getCodeMap().values().forEach(codeLines -> {
					codeLines.entrySet().forEach(entry -> {
						if (entry.getValue().contains(sliceMethodPrototype)) {
							System.out.printf("Found method signature in apk: %s, class: %s, line number: %d\n",
									apk.getName(), activity.getClassName(), entry.getKey());
							CodeBlock codeBlock = createCodeBlock(codeLines, entry.getKey());
							codeBlock.setApkName(apk.getName());
							codeBlock.setClassName(activity.getClassName());
							blocksToSlice.add(codeBlock);
						}
					});
				});
			});
			apk.getBroadcastReceivers().values().forEach(receiver -> {
				receiver.getCodeMap().values().forEach(codeLines -> {
					codeLines.entrySet().forEach(entry -> {
						if (entry.getValue().contains(sliceMethodPrototype)) {
							System.out.printf("Found method signature in apk: %s, class: %s, line number: %d\n",
									apk.getName(), receiver.getClassName(), entry.getKey());
							CodeBlock codeBlock = createCodeBlock(codeLines, entry.getKey());
							codeBlock.setApkName(apk.getName());
							codeBlock.setClassName(receiver.getClassName());
							blocksToSlice.add(codeBlock);
						}
					});
				});
			});
			apk.getContentProviders().values().forEach(contentProvider -> {
				contentProvider.getCodeMap().values().forEach(codeLines -> {
					codeLines.entrySet().forEach(entry -> {
						if (entry.getValue().contains(sliceMethodPrototype)) {
							System.out.printf("Found method signature in apk: %s, class: %s, line number: %d\n",
									apk.getName(), contentProvider.getClassName(), entry.getKey());
							CodeBlock codeBlock = createCodeBlock(codeLines, entry.getKey());
							codeBlock.setApkName(apk.getName());
							codeBlock.setClassName(contentProvider.getClassName());
							blocksToSlice.add(codeBlock);
						}
					});
				});
			});
			apk.getServices().values().forEach(service -> {
				service.getCodeMap().values().forEach(codeLines -> {
					codeLines.entrySet().forEach(entry -> {
						if (entry.getValue().contains(sliceMethodPrototype)) {
							System.out.printf("Found method signature in apk: %s, class: %s, line number: %d\n",
									apk.getName(), service.getClassName(), entry.getKey());
							CodeBlock codeBlock = createCodeBlock(codeLines, entry.getKey());
							codeBlock.setApkName(apk.getName());
							codeBlock.setClassName(service.getClassName());
							blocksToSlice.add(codeBlock);
						}
					});
				});
			});
		});

		System.out.println("\n### Creating a new slice for each found signature...");

		blocksToSlice.forEach(block -> {
			System.out.println("\nnew block...");

			Map<Integer, String> codeLines = block.getCodeLines();
			int firstLineNumber = getFirstLineNumber(block);
			int lastLineNumber = getLastLineNumber(block);

			// get registers for last line (slice start line)
			String lastLine = codeLines.get(lastLineNumber);
			block.getSlicedLines().put(lastLineNumber, lastLine);
			Set<String> registersToControl = getRegistersForLine(lastLine);
			Set<String> alreadyControlledRegisters = new HashSet<>();

			while (!registersToControl.isEmpty()) { // 				----> START <----
				String currentRegister = registersToControl.iterator().next();

				if (alreadyControlledRegisters.contains(currentRegister)) {
					System.out.println(" --> Skipping, register was already controlled: " + currentRegister);
					registersToControl.remove(currentRegister); // prevents infinite loops
					continue;
				}

				for (int i = lastLineNumber - 1; i >= firstLineNumber; i--) {
					String currentLine = codeLines.get(i);

					if (StringUtils.isBlank(currentLine)) { // skip if null or empty line
						continue;
					}
					currentLine = currentLine.trim();

					if (currentLine.startsWith("move-result-object " + currentRegister)
							|| currentLine.startsWith("move-result " + currentRegister)) {
						// get the previous non empty line
						int previousLineNumber = getPreviousNonEmptyLineNumber(block, i);

						// swap current register with new register
						Set<String> relatedRegisters = getRegistersForLine(block.getCodeLines().get(previousLineNumber));
						// add both lines to slice	
						block.getSlicedLines().put(i, currentLine);
						block.getSlicedLines().put(previousLineNumber, block.getCodeLines().get(previousLineNumber).trim());
						System.out.println("(" + currentRegister + ") " + i + "\t" + currentLine.trim());
						System.out.println(
								"(" + currentRegister + ") " + previousLineNumber + "\t" + block.getCodeLines().get(previousLineNumber).trim());
						registersToControl.remove(currentRegister);
						currentRegister = relatedRegisters.iterator().next(); // TODO check
						//						registersToControl.addAll(relatedRegisters);
						continue; // continue search from the previous line;
					}

					// get all registers that might effect the currentRegister
					Set<String> relatedRegisters = getRelatedRegisters(currentRegister, currentLine);

					if (!relatedRegisters.isEmpty()) { // means that our currentRegister was changed somehow
						block.getSlicedLines().put(i, currentLine); // add line to slice
						System.out.println("(" + currentRegister + ") " + i + "\t" + currentLine.trim());
						relatedRegisters.forEach(newReg -> { // add the other register, so that we can also track them
							registersToControl.add(newReg);
						});
					}
				}
				registersToControl.remove(currentRegister); // we got all the slices for one register, so delete it
				alreadyControlledRegisters.add(currentRegister); // prevent same register controls over and over
			} // 																							----> END <----
			System.out.println("already controlled registers: ");
			alreadyControlledRegisters.forEach(r -> System.out.println(r));
			System.out.println("end block...\n");
		});

		System.out.println("\n### Slicing is complete. Here are all the slices:");

		blocksToSlice.forEach(block -> {// print slice results for each block
			System.out.println("\n" + block.getApkName() + ", " + block.getClassName() + "\n" +
					block.getMethodDefinition());

			SortedSet<Integer> keys = new TreeSet<>(block.getSlicedLines().keySet());
			for (Integer key : keys) { // print lines in ascending order
				String line = block.getSlicedLines().get(key);
				System.out.println(key + "\t" + line.trim());
			}
		});

		writeSlicesToFile(blocksToSlice);
	}

	private int getPreviousNonEmptyLineNumber(CodeBlock block, int currentLineNumber) {
		for (int i = currentLineNumber - 1; i >= 1; i--) {
			if (StringUtils.isNoneBlank(block.getCodeLines().get(i))) {
				return i;
			}
		}
		throw new RuntimeException("Couldn't find previous line number");
	}

	private Set<String> getRelatedRegisters(String register, String line) {
		Set<String> newRegisters = new HashSet<>();
		if (StringUtils.isBlank(line)) {
			return newRegisters;
		}
		// trim is important! every line starts with whitespace!
		line = line.trim().replace("{", "").replace("}", ""); // make lines with {p0, v1, ...} also match
		// starts with: everything except a space, one space, everything
		Pattern p1 = Pattern.compile("^([^ ]*)([ ])(.*)");
		// create matcher for pattern p and given string
		Matcher m1 = p1.matcher(line);

		if (m1.find()) {
			//			System.out.println(" *** pattern found : " + m1.group(3));
			if (m1.group(3).startsWith(register)) {
				String[] possibleNewRegisters = m1.group(3).split(", ");
				for (String reg : possibleNewRegisters) {
					if (reg.length() == 2 && !line.startsWith("invoke-virtual")) { // TODO: bad cheat, might not work for everything
						newRegisters.add(reg);
					}
				}
			}
		}
		return newRegisters;
	}

	private int getFirstLineNumber(CodeBlock block) {
		// return the smallest integer key (the last line number)
		Optional<Integer> optInt = block.getCodeLines().keySet().stream().min(Comparator.naturalOrder());
		int firstLineNum = optInt.get(); // return the int if exist, an exception otherwise
		return firstLineNum;
	}

	private int getLastLineNumber(CodeBlock block) {
		// return the biggest integer key (the last line number)
		Optional<Integer> optInt = block.getCodeLines().keySet().stream().max(Comparator.naturalOrder());
		int lastLineNum = optInt.get(); // return the int if exist, an exception otherwise
		return lastLineNum;
	}

	private Set<String> getRegistersForLine(String line) {
		Set<String> registers = new HashSet<>();

		if (StringUtils.isBlank(line)) {
			return registers;
		}
		Pattern p = Pattern.compile("^(.*)[{](.*)[}](.*)");
		Matcher m = p.matcher(line);

		if (m.find()) {
			registers = new HashSet<>(Arrays.asList(m.group(2).split(", ")));
		}
		return registers;
	}

	private void writeSlicesToFile(List<CodeBlock> blocksToSlice) {
		StringBuilder builder = new StringBuilder();

		File sliceResultFile = new File(OUTPUT_FILE_NAME);
		blocksToSlice.forEach(block -> {
			builder.append(BLOCK_SEPARATOR + block.getApkName() + ", className: " + block.getClassName() + "\n");
			block.getCodeLines().entrySet().forEach(entry -> {
				builder.append("\n" + entry.getKey() + "\t" + entry.getValue());
			});
			try {
				FileUtils.writeStringToFile(sliceResultFile, builder.toString(), StandardCharsets.UTF_8);
			} catch (IOException e) {
				// TODO
				e.printStackTrace();
			}
		});
	}

	private CodeBlock createCodeBlock(Map<Integer, String> codeLines, Integer key) {
		CodeBlock codeBlock = new CodeBlock();

		for (int lineNum = key; lineNum >= 1; lineNum--) {
			String line = codeLines.get(lineNum);
			if (line.contains(METHOD_START)) {
				codeBlock.setMethodDefinition(lineNum + "\t" + line);
				return codeBlock;
			}
			if (!line.contains(".line")) {
				codeBlock.getCodeLines().put(lineNum, line);
			}
		}
		return codeBlock;
	}

}
