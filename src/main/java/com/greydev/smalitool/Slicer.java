package com.greydev.smalitool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.greydev.smalitool.model.Apk;

public class Slicer {

	private static final String METHOD_START = ".method";

	public static void slice(Map<String, Apk> apkList, String sliceMethodPrototype) {

		List<Map<Integer, String>> blocksToSlice = new ArrayList<>();
		//		Map<String, Map<Integer, String>> blocksToSlice = new HashMap<>();

		// search every class for this line
		apkList.values().forEach(apk -> {
			apk.getActivities().values().forEach(activity -> {

				activity.getCodeMap().values().forEach(codeLines -> {
					codeLines.entrySet().forEach(entry -> {
						if (entry.getValue().contains(sliceMethodPrototype)) {
							System.out.println(" *** FOUND IT *** key (line number):" + entry.getKey());
							// create blocks for each found line, every line till '.method'
							blocksToSlice.add(createSliceBlock(codeLines, entry.getKey()));
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

		System.out.println("\n Printing slice blocks: \n\n");
		blocksToSlice.forEach(map -> {
			System.out.println();
			map.entrySet().forEach(entry -> {
				System.out.println(entry.getKey() + "\t" + entry.getValue());
			});
			//			map.values().forEach(line -> System.out.println(line));
		});

		//		blocksToSlice.entrySet().forEach(entry -> {
		//			System.out.println("\n" + entry.getKey());
		//			entry.getValue().entrySet().forEach(codeLine -> {
		//				System.out.println(codeLine.getKey() + "\t" + codeLine.getValue());
		//			});
		//		});

		// ******************************************************************************************************

	}

	private static Map<Integer, String> createSliceBlock(Map<Integer, String> codeLines, Integer key) {

		Map<Integer, String> sliceBlock = new HashMap<>();

		for (int lineNum = key; lineNum >= 1; lineNum--) {
			String line = codeLines.get(lineNum);
			if (line.contains(METHOD_START)) {
				sliceBlock.put(lineNum, line);
				return sliceBlock;
			}
			sliceBlock.put(lineNum, line);
		}

		return sliceBlock;
	}

}
