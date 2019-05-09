package com.greydev.ms_project1;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.Node;

import com.greydev.ms_project1.model.Activity;
import com.greydev.ms_project1.model.Apk;
import com.greydev.ms_project1.model.BroadcastReceiver;
import com.greydev.ms_project1.model.ContentProvider;
import com.greydev.ms_project1.model.Service;

public class ApkInfoExtractor {

	private static final String MANIFEST_FILE_NAME = "AndroidManifest.xml";

	public static Apk extractApkFromSmaliFolder(String smaliFolderPath)
			throws FileNotFoundException, DocumentException {

		final AndroidManifestParser manifestParser = new AndroidManifestParser(smaliFolderPath + "\\" + MANIFEST_FILE_NAME);

		Apk apk = new Apk();
		apk.setSmaliFolderPath(smaliFolderPath);
		apk.setDecodedManifestFilePath(manifestParser.getManifestFile().getAbsolutePath());

		String packageName = manifestParser.getPackageName();
		// TODO remove app name?
		String appName = StringUtils.substringAfterLast(packageName, ".");
		apk.setPackageName(packageName);
		apk.setAppName(appName);

		System.out.println("\n\npackage: " + packageName + " *******************************");
		System.out.println("appName: " + appName + " *******************************");

		// get all activities
		List<Node> activityList = manifestParser.getActivities();
		System.out.println("ACTIVITY found: " + activityList.size());

		activityList.forEach(node -> {
			String fullClassName = manifestParser.getNodeName(node);
			String className = StringUtils.substringAfterLast(fullClassName, ".");

			List<String> smaliClassPathList = new ArrayList<>();
			try {
				smaliClassPathList = Util.recursiveSearch(className, new File(apk.getSmaliFolderPath()));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			List<String> intentList = manifestParser.getIntentActions(node);

			// some apk's have the same class names with different package names!
			apk.getActivities().put(fullClassName, new Activity(className, smaliClassPathList, intentList));
		});

		// get all services
		List<Node> serviceList = manifestParser.getServices();
		System.out.println("SERVICE found: " + serviceList.size());
		serviceList.forEach(node -> {

			String fullClassName = manifestParser.getNodeName(node);
			String className = StringUtils.substringAfterLast(fullClassName, ".");
			List<String> smaliClassPathList = new ArrayList<>();
			try {
				smaliClassPathList = Util.recursiveSearch(className, new File(apk.getSmaliFolderPath()));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			List<String> intentList = manifestParser.getIntentActions(node);
			apk.getServices().put(fullClassName, new Service(className, smaliClassPathList, intentList));
		});

		// get all brodcast receivers
		List<Node> receiverList = manifestParser.getBroadcastReceivers();
		System.out.println("BROADCAST RECEIVER found: " + receiverList.size());
		receiverList.forEach(node -> {

			String fullClassName = manifestParser.getNodeName(node);
			String className = StringUtils.substringAfterLast(fullClassName, ".");
			List<String> smaliClassPathList = new ArrayList<>();
			try {
				smaliClassPathList = Util.recursiveSearch(className, new File(apk.getSmaliFolderPath()));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			List<String> intentList = manifestParser.getIntentActions(node);
			apk.getBrodcastReceivers().put(fullClassName, new BroadcastReceiver(className, smaliClassPathList, intentList));
		});

		// get all providers
		List<Node> providerList = manifestParser.getContentProviders();
		System.out.println("PROVIDER found: " + providerList.size());
		providerList.forEach(node -> {

			String fullClassName = manifestParser.getNodeName(node);
			String className = StringUtils.substringAfterLast(fullClassName, ".");
			List<String> smaliClassPathList = new ArrayList<>();
			try {
				smaliClassPathList = Util.recursiveSearch(className, new File(apk.getSmaliFolderPath()));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			apk.getContentProviders().put(fullClassName, new ContentProvider(className, smaliClassPathList));
		});
		return apk;
	}

}
