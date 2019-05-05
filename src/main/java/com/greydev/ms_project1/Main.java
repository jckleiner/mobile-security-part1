package com.greydev.ms_project1;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/*
 * To copy any apk from the emulator to your current directory do the following in your terminal:
 * 		'adb shell pm list packages', copy the package name you want to pull 'com.instagram.android'
 * 		'adb shell pm path com.instagram.android'  , returns: package:/data/app/com.instagram.android-8rjwDMo7jo7ONJYp8RUX5Q==/base.apk
 * 		'adb pull /data/app/com.instagram.android-8rjwDMo7jo7ONJYp8RUX5Q==/base.apk .'
 *  DOES NOT WORK WITH GIT BASH ?
 */

public class Main {

	public static void main(String[] args) {

		// TODO full path vs relative path: abc/xxx, ./xxx, xxx
		// TODO problem when there is 2 apks with the same name

		if (args.length == 0 || args.length > 1) {
			System.out.println("Only one argument is expected. Found: " + args.length);
//			test();
			return;
		}
		System.out.println("input: '" + args[0] + "'");

		String targetFolderPath = null;

		if (args[0].startsWith("/") || args[0].startsWith("C:\\")) {
			targetFolderPath = args[0];
		}
		else {
			Path path = FileSystems.getDefault().getPath(".").toAbsolutePath();
			String firstPath = StringUtils.removeEnd(path.toString(), ".");
			System.out.println(StringUtils.removeEnd(path.toString(), "."));
//			System.out.println(path.resolve(args[0]));
			String secondPath = StringUtils.remove(args[0], "./");
//			System.out.println(path.resolve(StringUtils.remove(args[0], "./")));
			targetFolderPath = firstPath + secondPath;
		}
		System.out.println("parsed folder path: " + targetFolderPath + "\n");

		File targetFolder = new File(targetFolderPath);
		if (!targetFolder.isDirectory()) {
			System.out.println("File does not exist or is not a directory!");
			return;
		}

		List<File> targetFolderItems = Arrays.asList(targetFolder.listFiles());
		List<File> apkFileList = new ArrayList<>();
		targetFolderItems.forEach(item -> {
			if (item.isFile() && item.getName().endsWith(".apk")) {
				apkFileList.add(item);
			}
//			System.out.println(item.getName());
		});

		ProcessBuilder processBuilder = new ProcessBuilder();
		// sets the working directory for the processBuilder
		processBuilder.directory(targetFolder);

		List<Integer> exitCodeList = new ArrayList<>();
		System.out.println(apkFileList.size() + " apk files found.");
		apkFileList.forEach(apkFile -> {

			System.out.println(apkFile.getName() + "--- apktool d " + apkFile.getPath());
			processBuilder.command("cmd.exe", "/c", "apktool d " + apkFile.getPath());
			exitCodeList.add(startProcess(processBuilder));
		});

		int successCount = Collections.frequency(exitCodeList, 0);
		int failCount = Collections.frequency(exitCodeList, 1);
		System.out.println(MessageFormat.format("\nFound {0} apk(s)\nsuccessful decoded: {1}\nfailure: {2}",
				apkFileList.size(), successCount, failCount));
//		System.out
//				.println(MessageFormat.format("Successfully decoded {0} apk(s) out of {1}", successCount, apkFileList.size()));

		// TODO change this command so it works with macOs and with a full path to the apk folder as argument
//		processBuilder.command("cmd.exe", "/c", "dir");
		// sets the working directory for the process
//		processBuilder.directory(targetFolder);
//		startProcess(processBuilder);

	}

	// return code, "successfully compiles 20 out of 20 ..."
	private static int startProcess(ProcessBuilder processBuilder) {
		System.out.println("Starting process...");
		int exitCode = 1;
		try {
			// allows the error message generated from the process to be sent to input stream
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}

			// returns 0 on success, 1 on failure
			exitCode = process.waitFor();
			System.out.println("\nExited with error code : " + exitCode);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return exitCode;
	}
}
