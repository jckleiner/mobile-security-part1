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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.greydev.ms_project1.model.Activity;
import com.greydev.ms_project1.model.Apk;
import com.greydev.ms_project1.model.BroadcastReceiver;
import com.greydev.ms_project1.model.ContentProvider;
import com.greydev.ms_project1.model.Service;

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

	private static final String PREFIX_GENERATED = "generated_";

	public static void main(String[] args) {
		// TODO problem when there is 2 apks with the same name
		// TODO full path / relative path parsing might be error prone for different platforms

		System.out.println("user input: " + args[0]);

		if (args.length != 1) {
			System.out.println("Expecting only one argument");
			System.exit(0);
		}

		File targetFolder = getTargetFolder(args[0]);

		if (targetFolder == null) {
			System.out.println("Directory does not exist or is not a directory!");
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
			// TODO refactor so it also works with macOs
			String apkPath = apkFile.getPath();
			String generatedApkFolderName = PREFIX_GENERATED + StringUtils.removeEndIgnoreCase(apkFile.getName(), ".apk");
			String commandToExecute = MessageFormat.format("apktool d {0} -o {1}", apkPath, generatedApkFolderName);
			processBuilder.command("cmd.exe", "/c", commandToExecute);
			int exitCode = startProcess(processBuilder);

			if (exitCode == 0) {
				generatedFolderPaths.add(targetFolder.getAbsolutePath() + "\\" + generatedApkFolderName);
			}
			folderPathsToDelete.add(targetFolder.getAbsolutePath() + "\\" + generatedApkFolderName);
			exitCodes.add(exitCode);
		});

		int successCount = Collections.frequency(exitCodes, 0);
		int failCount = Collections.frequency(exitCodes, 1);
		System.out.println(MessageFormat.format("\nFound {0} apk(s)\nsuccessful decoded: {1}\nfailure: {2}", apkFiles.size(),
				successCount, failCount));

		System.out.println("generated file count: " + generatedFolderPaths.size());
		generatedFolderPaths.forEach(folderPath -> {
			System.out.println(folderPath);
		});

		List<Apk> apkList = new ArrayList<>();
		generatedFolderPaths.forEach(folderPath -> {
			Apk apk = parse(folderPath);
			if (apk != null) {
				apkList.add(apk);
			}
		});

		apkList.forEach(apk -> System.out.println(apk.toString()));

		deleteGeneratedFiles(folderPathsToDelete);
	}

	public static void deleteGeneratedFiles(List<String> generatedFolderPaths) {
		System.out.println("Deleting " + generatedFolderPaths.size() + " generated folders...");

		generatedFolderPaths.forEach(folderPath -> {
			File currentFolder = new File(folderPath);
			if (currentFolder.isDirectory()) {
				try {
					FileUtils.deleteDirectory(currentFolder);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		System.out.println("Deleted " + generatedFolderPaths.size() + " generated folders successfully.");
	}

	// TODO rename
	public static Apk parse(String folderPath) {
		File manifestFile = new File(folderPath + "\\AndroidManifest.xml");

		if (!manifestFile.isFile()) {
			System.out.println("Manifest file is not found.");
			return null;
		}

		Apk apk = new Apk();
		apk.setSmaliFolderPath(folderPath);
		apk.setDecodedManifestFilePath(manifestFile.getAbsolutePath());

		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(manifestFile); // a URL object can also be passed as an argument
		} catch (DocumentException e) {
			e.printStackTrace();
		}

		Element root = document.getRootElement(); // <manifest> element
		String packageName = root.valueOf("@package");
		String appName = StringUtils.substringAfterLast(packageName, ".");
		apk.setPackageName(packageName);
		apk.setAppName(appName);

		System.out.println("\n\npackage: " + packageName + " *******************************");
		System.out.println("appName: " + appName + " *******************************");

		Set<String> elementSet = new HashSet<>();
		// iterate through child elements of root
		for (Iterator<Element> it = root.elementIterator(); it.hasNext();) {
			Element element = it.next();
			//			elementSet.add(element.getName());
			//			System.out.println(element.getName());
		}

		List<Activity> activities = new ArrayList<>();
		List<BroadcastReceiver> brodcastReceivers = new ArrayList<>();
		List<ContentProvider> contentProviders = new ArrayList<>();
		List<Service> services = new ArrayList<>();

		// get all activities
		List<Node> activityList = document.selectNodes("//manifest/application/activity");
		System.out.println("ACTIVITY found: " + activityList.size());
		activityList.forEach(node -> {
			//			System.out.println(node.getName() + ": " + node.valueOf("@android:name"));
			String fullClassName = node.valueOf("@android:name");
			String className = StringUtils.substringAfterLast(fullClassName, ".");

			// find smaliClassPath
			// find intentFilterActions
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.directory(new File(apk.getSmaliFolderPath())); // sets the working directory for the processBuilder
			/* adding \ at the beginning of the class name eliminates files with slightly different names:
			   "WorkingActivity" vs "ServiceWorkingActivity" */
			processBuilder.command("cmd.exe", "/c", "dir /s /b | findstr \\" + className);
			List<String> smaliClassPathList = startProcessWithOutputList(processBuilder);
			// TODO assuming only a single line is returned when file is found.
			// TODO platform compatibility must be implemented. Commands, file separators etc.
			//			System.out.println("ACTIVITY: dir /s /b | findstr " + "\\" + className + " returns: ");
			//			smaliClassPathList.forEach(path -> System.out.println(path));

			// '//intent-filter/action' returns all nodes inside the xml file independent of the current node
			// 'intent-filter/action' returns only the child intent-filer/actions for the current node
			List<Node> intentActionList = node.selectNodes("intent-filter/action");
			List<String> intentList = new ArrayList<>();
			intentActionList.forEach(action -> {
				String intent = action.valueOf("@android:name");
				//				System.out.println("intent: " + intent);
				intentList.add(intent);
			});
			// some apks have the same class names with different package names!
			apk.getActivities().put(fullClassName, new Activity(className, smaliClassPathList, intentList));
		});

		// get all services
		List<Node> serviceList = document.selectNodes("//manifest/application/service");
		System.out.println("SERVICE found: " + serviceList.size());
		serviceList.forEach(node -> {

			String fullClassName = node.valueOf("@android:name");
			String className = StringUtils.substringAfterLast(fullClassName, ".");
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.directory(new File(apk.getSmaliFolderPath())); // sets the working directory for the processBuilder
			processBuilder.command("cmd.exe", "/c", "dir /s /b | findstr \\" + className);
			List<String> smaliClassPathList = startProcessWithOutputList(processBuilder);
			//			System.out.println("SERVICE: dir /s /b | findstr " + "\\" + className + " returns: ");
			//			smaliClassPathList.forEach(path -> System.out.println(path));

			// '//intent-filter/action' returns all nodes inside the xml file independent of the current node
			// 'intent-filter/action' returns only the child intent-filer/actions for the current node
			List<Node> intentActionList = node.selectNodes("intent-filter/action");
			List<String> intentList = new ArrayList<>();
			intentActionList.forEach(action -> {
				String intent = action.valueOf("@android:name");
				//				System.out.println("intent: " + intent);
				intentList.add(intent);
			});
			apk.getServices().put(fullClassName, new Service(className, smaliClassPathList, intentList));
		});

		// get all brodcast receivers
		List<Node> receiverList = document.selectNodes("//manifest/application/receiver");
		System.out.println("BROADCAST RECEIVER found: " + receiverList.size());
		receiverList.forEach(node -> {

			String fullClassName = node.valueOf("@android:name");
			String className = StringUtils.substringAfterLast(fullClassName, ".");
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.directory(new File(apk.getSmaliFolderPath())); // sets the working directory for the processBuilder
			processBuilder.command("cmd.exe", "/c", "dir /s /b | findstr \\" + className);
			List<String> smaliClassPathList = startProcessWithOutputList(processBuilder);
			//			System.out.println("SERVICE: dir /s /b | findstr " + "\\" + className + " returns: ");
			//			smaliClassPathList.forEach(path -> System.out.println(path));

			// '//intent-filter/action' returns all nodes inside the xml file independent of the current node
			// 'intent-filter/action' returns only the child intent-filer/actions for the current node
			List<Node> intentActionList = node.selectNodes("intent-filter/action");
			List<String> intentList = new ArrayList<>();
			intentActionList.forEach(action -> {
				String intent = action.valueOf("@android:name");
				//				System.out.println("intent: " + intent);
				intentList.add(intent);
			});
			apk.getBrodcastReceivers().put(fullClassName, new BroadcastReceiver(className, smaliClassPathList, intentList));

		});

		// get all providers
		List<Node> providerList = document.selectNodes("//manifest/application/provider");
		System.out.println("PROVIDER found: " + providerList.size());
		providerList.forEach(node -> {

			String fullClassName = node.valueOf("@android:name");
			String className = StringUtils.substringAfterLast(fullClassName, ".");
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.directory(new File(apk.getSmaliFolderPath())); // sets the working directory for the processBuilder
			processBuilder.command("cmd.exe", "/c", "dir /s /b | findstr \\" + className);
			List<String> smaliClassPathList = startProcessWithOutputList(processBuilder);
			//			System.out.println("SERVICE: dir /s /b | findstr " + "\\" + className + " returns: ");
			//			smaliClassPathList.forEach(path -> System.out.println(path));

			apk.getContentProviders().put(fullClassName, new ContentProvider(className, smaliClassPathList));

		});
		return apk;
	}

	private static File getTargetFolder(String folderPath) {
		if (!(folderPath.startsWith("/") || folderPath.startsWith("C:\\"))) {
			Path path = FileSystems.getDefault().getPath(".").toAbsolutePath(); // adds a . at the end
			String currentDirectoryPath = StringUtils.removeEnd(path.toString(), ".");
			String userInput = StringUtils.remove(folderPath, "./"); // remove ./ if present
			folderPath = currentDirectoryPath + userInput;
		}
		System.out.println("Searching for folder: " + folderPath + "\n");

		File targetFolder = new File(folderPath);
		if (!targetFolder.isDirectory()) {
			return null;
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
			//			System.out.println("\nExited with error code : " + exitCode);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return exitCode;
	}

	private static List<String> startProcessWithOutputList(ProcessBuilder processBuilder) {
		int exitCode = 1;
		List<String> consoleOutputLines = new ArrayList<>();
		try {
			// allows the error message generated from the process to be sent to input stream
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				//				System.out.println(line);
				consoleOutputLines.add(line);
			}

			exitCode = process.waitFor(); // returns 0 on success, 1 on failure
			//			System.out.println("\nExited with error code : " + exitCode);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return consoleOutputLines;
	}

}
