package com.greydev.smalitool;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.greydev.smalitool.model.Apk;
import com.greydev.smalitool.model.CodeBlock;

public class Slicer {

	private static final String METHOD_START = ".method";
	private static final String BLOCK_SEPARATOR = "\n\n######################################################"
			+ "######################################\n";

	public static void slice(Map<String, Apk> apkList, String sliceMethodPrototype) {

		//		List<Map<Integer, String>> blocksToSlice = new ArrayList<>();
		List<CodeBlock> blocksToSlice = new ArrayList<>();
		//		Map<String, Map<Integer, String>> blocksToSlice = new HashMap<>();

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
			//			apk.getBroadcastReceivers().values().forEach(broadcastReceiver -> {
			//				broadcastReceiver.getCodeMap().values().forEach(codeList -> {
			//					codeList.forEach(line -> {
			//
			//					});
			//
			//				});
			//			});
			//			apk.getContentProviders().values().forEach(contentProvider -> {
			//				contentProvider.getCodeMap().values().forEach(codeList -> {
			//					codeList.forEach(line -> {
			//
			//					});
			//
			//				});
			//			});
			//			apk.getServices().values().forEach(service -> {
			//				service.getCodeMap().values().forEach(codeList -> {
			//					codeList.forEach(line -> {
			//
			//					});
			//
			//				});
			//			});
		});
		writeSlicesToFile(blocksToSlice);

		//		blocksToSlice.entrySet().forEach(entry -> {
		//			System.out.println("\n" + entry.getKey());
		//			entry.getValue().entrySet().forEach(codeLine -> {
		//				System.out.println(codeLine.getKey() + "\t" + codeLine.getValue());
		//			});
		//		});

		// ******************************************************************************************************

	}

	private static void writeSlicesToFile(List<CodeBlock> blocksToSlice) {
		StringBuilder builder = new StringBuilder();

		File sliceResultFile = new File(System.getProperty("user.home") + "\\smalitool_slices.txt");
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

	private static CodeBlock createCodeBlock(Map<Integer, String> codeLines, Integer key) {
		CodeBlock codeBlock = new CodeBlock();

		for (int lineNum = key; lineNum >= 1; lineNum--) {
			String line = codeLines.get(lineNum);
			if (line.contains(METHOD_START)) {
				codeBlock.getCodeLines().put(lineNum, line);
				return codeBlock;
			}
			codeBlock.getCodeLines().put(lineNum, line);
		}

		return codeBlock;
	}

}
