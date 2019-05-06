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

	private static final String PREFIX_GENERATED = "generated_";

	public static void main(String[] args) {
		// TODO problem when there is 2 apks with the same name
		// TODO full path / relative path parsing might be error prone for different platforms

		System.out.println("user input: '" + args[0] + "'");

		File targetFolder = getTargetFolder(args);

		List<File> targetFolderItems = Arrays.asList(targetFolder.listFiles());
		List<File> apkFileList = new ArrayList<>();
		List<Integer> exitCodeList = new ArrayList<>();

		targetFolderItems.forEach(item -> {
			if (item.isFile() && item.getName().endsWith(".apk")) {
				apkFileList.add(item);
			}
		});

		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(targetFolder); // sets the working directory for the processBuilder

		apkFileList.forEach(apkFile -> {
			// TODO refactor so it also works with macOs
			String apkPath = apkFile.getPath();
			String generatedApkFolderName = PREFIX_GENERATED + StringUtils.removeEnd(apkFile.getName(), ".apk");
			String commandToExecute = MessageFormat.format("apktool d {0} -o {1}", apkPath, generatedApkFolderName);
			processBuilder.command("cmd.exe", "/c", commandToExecute);
			int exitCode = startProcess(processBuilder);
			exitCodeList.add(exitCode);
		});

		int successCount = Collections.frequency(exitCodeList, 0);
		int failCount = Collections.frequency(exitCodeList, 1);
		System.out.println(MessageFormat.format("\nFound {0} apk(s)\nsuccessful decoded: {1}\nfailure: {2}",
				apkFileList.size(), successCount, failCount));

	}

	private static File getTargetFolder(String[] args) {
		String targetFolderPath = null;

		if (args.length != 1) {
			System.out.println("Expecting only one argument");
			System.exit(0);
		}
		if (args[0].startsWith("/") || args[0].startsWith("C:\\")) {
			targetFolderPath = args[0];
		}
		else {
			Path path = FileSystems.getDefault().getPath(".").toAbsolutePath(); // adds a . at the end
			String currentDirectoryPath = StringUtils.removeEnd(path.toString(), ".");
			System.out.println(currentDirectoryPath);
			String userInput = StringUtils.remove(args[0], "./");
			targetFolderPath = currentDirectoryPath + userInput;
		}

		System.out.println("parsed folder path: " + targetFolderPath + "\n");

		File targetFolder = new File(targetFolderPath);
		if (!targetFolder.isDirectory()) {
			System.out.println("Directory does not exist or is not a directory!");
			System.exit(0);
		}
		return targetFolder;
	}

	private static int startProcess(ProcessBuilder processBuilder) {
		System.out.println("Starting process...");
		int exitCode = 1;
		try {
			processBuilder.redirectErrorStream(true); // allows the error message generated from the process to be sent to input stream
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}

			exitCode = process.waitFor(); // returns 0 on success, 1 on failure
			System.out.println("\nExited with error code : " + exitCode);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return exitCode;
	}
}
