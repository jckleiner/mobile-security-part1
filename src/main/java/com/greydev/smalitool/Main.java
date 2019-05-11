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
 * TODO maybe: tool -delete -gui <folderPath>
 * TODO List<String> smaliClassPath could also be a HashMap<String, String/File>
 */
public class Main {

	private static final Logger LOG = Util.getConfiguredLogger(Main.class);
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

		// **********************************************************************************************

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
			// TODO refactor so it also works with macOs
			String apkPath = apkFile.getPath();
			String generatedApkFolderName = PREFIX_GENERATED + StringUtils.removeEndIgnoreCase(apkFile.getName(), ".apk");
			String commandToExecute = MessageFormat.format("apktool d {0} -o {1}", apkPath, generatedApkFolderName);
			processBuilder.command("cmd.exe", "/c", commandToExecute);
			int exitCode = Util.startProcess(processBuilder);

			if (exitCode == 0) {
				generatedFolderPaths.add(targetFolder.getAbsolutePath() + "\\" + generatedApkFolderName);
			}
			folderPathsToDelete.add(targetFolder.getAbsolutePath() + "\\" + generatedApkFolderName);
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

		List<Apk> apkList = new ArrayList<>();
		generatedFolderPaths.forEach(apkFolderPath -> {
			Apk apk = null;
			try {
				apk = ApkInfoExtractor.extractApkFromSmaliFolder(apkFolderPath);
			} catch (FileNotFoundException | DocumentException e) {
				e.printStackTrace();
			}
			if (apk != null) {
				apkList.add(apk);
			}
		});
		apkList.forEach(apk -> LOG.info(apk.toString()));
		FileSystem.deleteFiles(folderPathsToDelete);
	}

}
