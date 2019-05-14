package com.greydev.smalitool;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.slf4j.Logger;

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
 * TODO List<String> smaliClassPath could also be a HashMap<String, String/File>
 */
public class Main {

	private static final Logger LOG = Utils.getConfiguredLogger(Main.class);
	private static final String PREFIX_GENERATED = "generated_";

	public static void main(String[] args) {

		if (args.length != 1) {
			LOG.info("Expecting only one argument");
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
		List<Integer> exitCodes = new ArrayList<>();

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
			exitCodes.add(exitCode);
		});

		int successCount = Collections.frequency(exitCodes, 0);
		int failCount = Collections.frequency(exitCodes, 1);
		LOG.info(MessageFormat.format("\nFound {0} apk(s)\nsuccessful decoded: {1}\nfailure: {2}", apkFiles.size(),
				successCount, failCount));

		LOG.info("generated file count: " + generatedFolderPaths.size());
		generatedFolderPaths.forEach(folderPath -> {
			LOG.info(folderPath);
		});

		// extract apks from all the generated smali folders
		List<Apk> apkList = new ArrayList<>();
		for (String apkFolderPath : generatedFolderPaths) {
			Apk apk = null;
			try {
				ApkInfoExtractor apkExtractor = new ApkInfoExtractor();
				apk = apkExtractor.extractApkFromSmaliFolder(apkFolderPath);
			} catch (FileNotFoundException | DocumentException e) {
				e.printStackTrace();
				LOG.error(e.getStackTrace().toString());
			}
			if (apk != null) {
				apkList.add(apk);
			}
		}
		apkList.forEach(apk -> LOG.info(apk.toString()));

		apkList = null;
		System.gc();
		// delete all apk's before program closes?

		// TODO if they already exist then don't delete, show error message?
		FileSystem.deleteFiles(folderPathsToDelete);
	}

}
