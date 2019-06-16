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
import java.util.TreeMap;
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

		// search every class for this line
		apkList.values().forEach(apk -> {
			apk.getActivities().values().forEach(activity -> {
				activity.getCodeMap().values().forEach(codeLines -> {
					codeLines.entrySet().forEach(entry -> {
						if (entry.getValue().contains(sliceMethodPrototype)) {
							System.out.println(" *** FOUND IT *** key (line number):" + entry.getKey());
							System.out.println("Apk: " + apk.getName() + ", class: " + activity.getClassName());
							// create blocks for each found line, every line till '.method'
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
							System.out.println(" *** FOUND IT *** key (line number):" + entry.getKey());
							System.out.println("Apk: " + apk.getName() + ", class: " + receiver.getClassName());
							// create blocks for each found line, every line till '.method'
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
							System.out.println(" *** FOUND IT *** key (line number):" + entry.getKey());
							System.out.println("Apk: " + apk.getName() + ", class: " + contentProvider.getClassName());
							// create blocks for each found line, every line till '.method'
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
							System.out.println(" *** FOUND IT *** key (line number):" + entry.getKey());
							System.out.println("Apk: " + apk.getName() + ", class: " + service.getClassName());
							// create blocks for each found line, every line till '.method'
							CodeBlock codeBlock = createCodeBlock(codeLines, entry.getKey());
							codeBlock.setApkName(apk.getName());
							codeBlock.setClassName(service.getClassName());
							blocksToSlice.add(codeBlock);
						}
					});
				});
			});
		});

		//TODO do correct slicing
		//		blocksToSlice.forEach(block -> {
		//			slices.add(sliceBackwards(block));
		//		});

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

			//			for (String reg : registersToControl) { 
			while (!registersToControl.isEmpty()) { // 				----> START <----
				String reg = registersToControl.iterator().next();

				if (alreadyControlledRegisters.contains(reg)) {
					System.out.println(" --> Skipping, register was already controlled: " + reg);
					continue;
				}

				for (int i = lastLineNumber - 1; i >= firstLineNumber; i--) {
					String currentLine = codeLines.get(i);
					Set<String> relatedRegisters = getRelatedRegister(reg, currentLine);

					if (!relatedRegisters.isEmpty()) {
						//						System.out.println(currentLine);
						block.getSlicedLines().put(i, currentLine);
						relatedRegisters.forEach(newReg -> {
							//							System.out.println("was looking for: " + reg + ", adding new register: " + newReg);
							registersToControl.add(newReg);
						});
					}
				}
				// TODO burda silmem for loopundaki sıraya bir etki eder mi? EXCEPTION ATIYOR
				// TODO add while in for loop works, remove throws exc ???
				registersToControl.remove(reg);
				alreadyControlledRegisters.add(reg);
			}
			System.out.println("already controlled registers: ");
			alreadyControlledRegisters.forEach(r -> System.out.println(r));
			System.out.println("end block...\n");
		});

		//

		// print slice results for each block
		blocksToSlice.forEach(block -> {

			block.getSlicedLines().keySet();

			SortedSet<Integer> keys = new TreeSet<>(block.getSlicedLines().keySet());
			for (Integer key : keys) {
				String line = block.getSlicedLines().get(key);
				// do something
				System.out.println(key + "\t" + line);
			}

			System.out.println("\n" + block.getApkName() + ", " + block.getClassName() + ", " +
					block.getMethodDefinition());
		});

		TreeMap<Integer, String> treeMap = new TreeMap<>();

		writeSlicesToFile(blocksToSlice);
	}

	private Set<String> getRelatedRegister(String register, String line) {
		Set<String> newRegisters = new HashSet<>();

		if (StringUtils.isBlank(line)) {
			return newRegisters;
		}
		// starts with: everything except a space, one space, everything
		Pattern p = Pattern.compile("^([^ ]*)([ ])(.*)");
		// create matcher for pattern p and given string
		Matcher m = p.matcher(line.trim()); // important! every line starts with whitespace!

		// if an occurrence if a pattern was found in a given string...
		if (m.find()) {
			if (m.group(3).startsWith(register)) {
				// TODO add other registers
				// TODO also need to check for {xx, xx, xx} and method names etc
				// mul-int v3, v3, v1 ---- if-gt v1, v0, :cond_0
				String[] possibleNewRegisters = m.group(3).split(", ");
				for (String reg : possibleNewRegisters) {
					if (reg.length() == 2) { // TODO: bad cheat, might not work for everything
						newRegisters.add(reg);
						// TODO also check for { } registers
					}
				}
			}
		}

		//		System.out.println("=== TEST === looking for: " + register);
		//		System.out.println("current line: " + line);
		//		newRegisters.forEach(reg -> System.out.println(reg + " - "));
		//		System.out.println();

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

		Pattern p2 = Pattern.compile("^(.*)[{](.*)[}](.*)");
		//		line = "invoke-virtual {p0, v2, v3}, Lcom/example/slicer_test/MainActivity;->writeWithTwoParams(II)V";
		Matcher m2 = p2.matcher(line);

		if (m2.find()) {
			registers = new HashSet<>(Arrays.asList(m2.group(2).split(", ")));
			//			registers = m2.group(2).split(", ");
			//			System.out.println("\n----------REGEX 2---------\n");
			//			System.out.println(m2.group(0)); // whole matched expression
			//			System.out.println(m2.group(1)); // first expression from round brackets (Testing)
			//			System.out.println(m2.group(2)); // second one (123)
			//			System.out.println(m2.group(3)); // third one (Testing)
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
