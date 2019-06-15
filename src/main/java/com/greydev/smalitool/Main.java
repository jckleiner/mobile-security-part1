package com.greydev.smalitool;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.slf4j.Logger;

import com.greydev.smalitool.model.Activity;
import com.greydev.smalitool.model.Apk;

/*
 * To copy any apk from the emulator to your current directory do the following in your terminal:
 * 		'adb shell pm list packages', copy the package name you want to pull 'com.instagram.android'
 * 		'adb shell pm path com.instagram.android'  , returns: package:/data/app/com.instagram.android-8rjwDMo7jo7ONJYp8RUX5Q==/base.apk
 * 		'adb pull /data/app/com.instagram.android-8rjwDMo7jo7ONJYp8RUX5Q==/base.apk .'
 *  DOES NOT WORK WITH GIT BASH ?
 */

/*
 * TODO maybe: smalitool -delete -gui <folderPath>
 */
public class Main {

	private static final Logger LOG = Utils.getConfiguredLogger(Main.class);
	private static final Formatter FORMATTER = new Formatter();
	public static final String PREFIX_GENERATED = "generated_";

	public static void main(String[] args) {

		//		if (args.length != 1) {
		//			LOG.info("Expecting only one argument");
		//			System.exit(0);
		//		}
		if (args.length != 2) {
			LOG.info("Please enter 2 arguments");
			System.exit(0);
		}
		LOG.info("input: {}", args[0]);

		File targetFolder = FileSystem.getFolder(args[0]);

		if (targetFolder == null) {
			LOG.info("Directory does not exist or is not a directory!");
			System.exit(0);
		}

		List<File> targetFolderContent = Arrays.asList(targetFolder.listFiles());
		List<File> apkFiles = new ArrayList<>();
		List<String> generatedFolderPaths = new ArrayList<>();
		List<String> folderPathsToDelete = new ArrayList<>();

		targetFolderContent.forEach(item -> {
			if (item.isFile() && (StringUtils.endsWithIgnoreCase(item.getName(), ".apk"))) {
				apkFiles.add(item);
			}
		});

		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(targetFolder); // sets the working directory for the processBuilder

		apkFiles.forEach(apkFile -> {
			String apkPath = apkFile.getPath();
			String generatedApkFolderName = PREFIX_GENERATED + StringUtils.removeEndIgnoreCase(apkFile.getName(), ".apk");
			String commandToExecute = MessageFormat.format("apktool d {0} -o {1}", apkPath, generatedApkFolderName);
			if (Utils.isOsWindows()) {
				processBuilder.command("cmd.exe", "/c", commandToExecute);
			}
			else if (Utils.isOsUnixBased()) {
				processBuilder.command("/bin/bash", "-c", commandToExecute);
			}
			int exitCode = Utils.startProcessWithExitCode(processBuilder);

			if (exitCode == 0) { // success
				generatedFolderPaths.add(targetFolder.getAbsolutePath() + File.separator + generatedApkFolderName);
			}
			// apktool still generates an empty folder on any error
			folderPathsToDelete.add(targetFolder.getAbsolutePath() + File.separator + generatedApkFolderName);
		});

		//		int failCount = Collections.frequency(exitCodes, 1); // TODO remove this commented-out line
		LOG.info("{}", FORMATTER.format("%nFound %d apk(s) in the destination folder%nsuccessfully decoded: %d%nfailure: %d%n",
				apkFiles.size(), generatedFolderPaths.size(), apkFiles.size() - generatedFolderPaths.size()));

		LOG.info("Generated Folder(s):");
		generatedFolderPaths.forEach(LOG::info);

		// extract apks from all the generated smali folders
		Map<String, Apk> apkList = new HashMap<>();
		for (String apkFolderPath : generatedFolderPaths) {
			Apk apk = null;
			try {
				ApkInfoExtractor apkExtractor = new ApkInfoExtractor();
				apk = apkExtractor.extractApkFromSmaliFolder(apkFolderPath);
			} catch (FileNotFoundException | DocumentException e) {
				LOG.error(Arrays.toString(e.getStackTrace()));
			}
			if (apk != null) {
				String apkName = StringUtils.substringAfter(apkFolderPath, PREFIX_GENERATED);
				apkList.put(apkName, apk);
			}
		}
		apkList.values().forEach(apk -> LOG.info(apk.toString()));

		//		Apk instagramApk = apkList.get("instagram"); // TODO remove this commented-out block of code
		//		System.out.println(instagramApk.toString());
		// Lcom/instagram/direct/share/handler/DirectShareHandlerActivity;->getIntent()Landroid/content/Intent;
		//		Activity myActivity = instagramApk.getActivities().get("com.instagram.direct.share.handler.DirectShareHandlerActivity");
		//		myActivity.printCodeForSmaliClass("DirectShareHandlerActivity.smali");

		Apk slicerApk = apkList.get("slicertest"); // TODO remove this commented-out block of code
		// Lcom/example/slicer_test/MainActivity;->write(I)V
		// Lcom/instagram/direct/share/handler/DirectShareHandlerActivity;->getIntent()Landroid/content/Intent;
		Activity myActivity = slicerApk.getActivities().get("com.example.slicer_test.MainActivity");
		myActivity.printCodeForSmaliClass("MainActivity.smali");

		// ******************************************************************************************************
		String sliceMethodPrototype = args[1];
		System.out.println("\nMethod input: " + sliceMethodPrototype);
		System.out.println("\nStarting slicing...");
		Slicer.slice(apkList, sliceMethodPrototype);

		// ******************************************************************************************************

		// TODO if they already exist then don't delete, show error message?
		FileSystem.deleteFiles(folderPathsToDelete);
	}

}
