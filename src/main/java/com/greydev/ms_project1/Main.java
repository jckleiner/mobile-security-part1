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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/*
 * To copy any apk from the emulator to your current directory do the following in your terminal:
 * 		'adb shell pm list packages', copy the package name you want to pull 'com.instagram.android'
 * 		'adb shell pm path com.instagram.android'  , returns: package:/data/app/com.instagram.android-8rjwDMo7jo7ONJYp8RUX5Q==/base.apk
 * 		'adb pull /data/app/com.instagram.android-8rjwDMo7jo7ONJYp8RUX5Q==/base.apk .'
 *  DOES NOT WORK WITH GIT BASH ?
 */

public class Main {

	private static final String PREFIX_GENERATED = "generated_";

	public static void main(String[] args) throws DocumentException {
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

		File manifestFile = new File("C:\\Users\\Can\\Desktop\\develop\\MobileSecurity\\instagram\\AndroidManifest.xml");
		parse(manifestFile);
	}

	public static void parse(File manifestFile) throws DocumentException {

		if (!manifestFile.isFile()) {
			System.out.println("Manifest file is not found.");
			return;
		}

		SAXReader reader = new SAXReader();
		Document document = reader.read(manifestFile); // a URL object can also be passed as an argument

		Element root = document.getRootElement(); // <manifest> element

		// <manifest> can contain:
		// <compatible-screens>
		// <instrumentation>
		// <permission>
		// <permission-group>
		// <permission-tree>
		// <supports-gl-texture>
		// <supports-screens>
		// <uses-configuration>
		// <uses-feature>
		// <uses-permission>
		// <uses-permission-sdk-23>
		// <uses-sdk>
		// <application>

		// <application> can contain:
		// <activity>
		// <activity-alias>
		// <meta-data>
		// <service>
		// <receiver>
		// <provider>
		// <uses-library>

		// <activity> can contain:
		// <intent-filter>
		// <meta-data>
		// <layout>

		Set<String> elementSet = new HashSet<>();
		// iterate through child elements of root
		for (Iterator<Element> it = root.elementIterator(); it.hasNext();) {
			Element element = it.next();
//			elementSet.add(element.getName());
//			System.out.println(element.getName());
		}

		// get all activities
		List<Node> activityList = document.selectNodes("//manifest/application/activity");
		activityList.forEach(node -> {
			System.out.println(node.getName() + ": " + node.valueOf("@android:name"));
		});

		// get all services
		List<Node> serviceList = document.selectNodes("//manifest/application/service");
		serviceList.forEach(node -> {
			System.out.println(node.getName() + ": " + node.valueOf("@android:name"));
		});

		// get all brodcast receivers
		List<Node> receiverList = document.selectNodes("//manifest/application/receiver");
		receiverList.forEach(node -> {
			System.out.println(node.getName() + ": " + node.valueOf("@android:name"));
		});

		// get all providers
		List<Node> providerList = document.selectNodes("//manifest/application/provider");
		providerList.forEach(node -> {
			System.out.println(node.getName() + ": " + node.valueOf("@android:name"));
		});

		// iterate through child elements of root with element name "foo"
		// NPE
		for (Iterator<Element> it = root.element("application").elementIterator(); it.hasNext();) {
			Element element = it.next();
			elementSet.add(element.getName());
		}

		// element.valueOf("@android:name");

		// iterate through attributes of root
		for (Iterator<Attribute> it = root.attributeIterator(); it.hasNext();) {
			Attribute attribute = it.next();
			// do something
		}
//		elementSet.forEach(e -> System.out.println(e));
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
