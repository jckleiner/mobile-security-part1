package com.greydev.smalitool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.slf4j.Logger;

import com.greydev.smalitool.model.Activity;
import com.greydev.smalitool.model.Apk;
import com.greydev.smalitool.model.BroadcastReceiver;
import com.greydev.smalitool.model.ContentProvider;
import com.greydev.smalitool.model.Service;

public class ApkInfoExtractor {

	private static final Logger LOG = Utils.getConfiguredLogger(ApkInfoExtractor.class);
	private static final Formatter FORMATTER = new Formatter();
	private static final String MANIFEST_FILE_NAME = "AndroidManifest.xml";
	
	private static final String SMALIFOLDERPATHNULLTEXT = "smaliFolderPath cannot be null";

	private AndroidManifestParser manifestParser;

	/**
	 * 
	 * @param smaliFolderPath The path to smali files representing an APK.
	 * @return The {@code Apk}-object representing the APK found at the location the path leads to.
	 * @throws FileNotFoundException
	 * @throws DocumentException
	 */
	public Apk extractApkFromSmaliFolder(String smaliFolderPath)
			throws FileNotFoundException, DocumentException {
		Objects.requireNonNull(smaliFolderPath, SMALIFOLDERPATHNULLTEXT);

		manifestParser = new AndroidManifestParser(smaliFolderPath + File.separator + MANIFEST_FILE_NAME);

		Apk apk = new Apk();
		apk.setName(StringUtils.substringAfterLast(smaliFolderPath, Main.PREFIX_GENERATED));
		apk.setSmaliFolderPath(smaliFolderPath);
		apk.setDecodedManifestFilePath(manifestParser.getManifestFile().getAbsolutePath());
		apk.setPackageName(manifestParser.getPackageName());
		apk.setPermissions(manifestParser.getPermissions());

		LOG.info("{}", FORMATTER.format("%n%n******* %s *******", apk.getName()));
		
		LOG.info("{}", FORMATTER.format("Permissions: %d", apk.getPermissions().size()));

		List<Node> activityNodes = manifestParser.getActivities();
		apk.setActivities(extractActivities(activityNodes, apk.getSmaliFolderPath()));

		List<Node> broadcastReceiverNodes = manifestParser.getBroadcastReceivers();
		apk.setBroadcastReceivers(extractBroadcastReceivers(broadcastReceiverNodes, apk.getSmaliFolderPath()));

		List<Node> contentProviderNodes = manifestParser.getContentProviders();
		apk.setContentProviders(extractContentProviders(contentProviderNodes, apk.getSmaliFolderPath()));

		List<Node> serviceNodes = manifestParser.getServices();
		apk.setServices(extractServices(serviceNodes, apk.getSmaliFolderPath()));

		return apk;
	}

	/**
	 * Extracts the activities from a decompiled APK
	 * @param activityNodes A list of activities as parsed from the Android manifest file.
	 * @param smaliFolderPath The path to smali files representing an APK.
	 * @return Map of Activities with their fully qualified name as key
	 */
	private HashMap<String, Activity> extractActivities(List<Node> activityNodes, String smaliFolderPath) {
		Objects.requireNonNull(activityNodes, "activityNodes cannot be null");
		Objects.requireNonNull(smaliFolderPath, SMALIFOLDERPATHNULLTEXT);
		// get all activities
		LOG.info("{}", FORMATTER.format("Activities: %d", activityNodes.size()));
		HashMap<String, Activity> activities = new HashMap<>();

		for (Node node : activityNodes) {
			String fullClassName = manifestParser.getNodeName(node);
			String className = StringUtils.substringAfterLast(fullClassName, ".");
			List<String> smaliClassPathList = new ArrayList<>();
			try {
				smaliClassPathList = FileSystem.recursiveSearch(className, new File(smaliFolderPath));
			} catch (FileNotFoundException e) {
				LOG.error(Arrays.toString(e.getStackTrace()));
			}
			List<String> intentList = manifestParser.getIntentActions(node);
			Map<String, List<String>> codeMap = extractCode(smaliClassPathList);
			// some apk's have the same class names with different package names, that's why use full class names as key!
			activities.put(fullClassName, new Activity(className, codeMap, intentList));
		}
		return activities;
	}

	private HashMap<String, BroadcastReceiver> extractBroadcastReceivers(List<Node> receiverNodes, String smaliFolderPath) {
		Objects.requireNonNull(receiverNodes, "receiverNodes cannot be null");
		Objects.requireNonNull(smaliFolderPath, SMALIFOLDERPATHNULLTEXT);
		// get all broadcast receivers
		HashMap<String, BroadcastReceiver> broadcastReceivers = new HashMap<>();
		LOG.info("{}", FORMATTER.format("Broadcast Receivers: %d", receiverNodes.size()));

		receiverNodes.forEach(node -> {
			String fullClassName = manifestParser.getNodeName(node);
			String className = StringUtils.substringAfterLast(fullClassName, ".");
			List<String> smaliClassPathList = new ArrayList<>();
			try {
				smaliClassPathList = FileSystem.recursiveSearch(className, new File(smaliFolderPath));
			} catch (FileNotFoundException e) {
				LOG.error(Arrays.toString(e.getStackTrace()));
			}
			List<String> intentList = manifestParser.getIntentActions(node);
			Map<String, List<String>> codeMap = extractCode(smaliClassPathList);
			broadcastReceivers.put(fullClassName, new BroadcastReceiver(className, codeMap, intentList));
		});
		return broadcastReceivers;
	}

	private HashMap<String, ContentProvider> extractContentProviders(List<Node> providerNodes, String smaliFolderPath) {
		Objects.requireNonNull(providerNodes, "providerNodes cannot be null");
		Objects.requireNonNull(smaliFolderPath, SMALIFOLDERPATHNULLTEXT);
		// get all providers
		HashMap<String, ContentProvider> contentProviders = new HashMap<>();
		LOG.info("{}", FORMATTER.format("Content Providers: %d", providerNodes.size()));

		providerNodes.forEach(node -> {
			String fullClassName = manifestParser.getNodeName(node);
			String className = StringUtils.substringAfterLast(fullClassName, ".");
			List<String> smaliClassPathList = new ArrayList<>();
			try {
				smaliClassPathList = FileSystem.recursiveSearch(className, new File(smaliFolderPath));
			} catch (FileNotFoundException e) {
				LOG.error(Arrays.toString(e.getStackTrace()));
			}
			Map<String, List<String>> codeMap = extractCode(smaliClassPathList);
			contentProviders.put(fullClassName, new ContentProvider(className, codeMap));
		});
		return contentProviders;
	}

	private HashMap<String, Service> extractServices(List<Node> serviceNodes, String smaliFolderPath) {
		Objects.requireNonNull(serviceNodes, "serviceNodes cannot be null");
		Objects.requireNonNull(smaliFolderPath, SMALIFOLDERPATHNULLTEXT);
		// get all services
		HashMap<String, Service> services = new HashMap<>();
		LOG.info("{}", FORMATTER.format("Services: %d", serviceNodes.size()));

		serviceNodes.forEach(node -> {
			String fullClassName = manifestParser.getNodeName(node);
			String className = StringUtils.substringAfterLast(fullClassName, ".");
			List<String> smaliClassPathList = new ArrayList<>();
			try {
				smaliClassPathList = FileSystem.recursiveSearch(className, new File(smaliFolderPath));
			} catch (FileNotFoundException e) {
				LOG.error(Arrays.toString(e.getStackTrace()));
			}
			List<String> intentList = manifestParser.getIntentActions(node);
			Map<String, List<String>> codeMap = extractCode(smaliClassPathList);
			services.put(fullClassName, new Service(className, codeMap, intentList));
		});
		return services;
	}

	private Map<String, List<String>> extractCode(List<String> smaliClassPathList) {
		Objects.requireNonNull(smaliClassPathList, "smaliClassPathList cannot be null");
		Map<String, List<String>> codeMap = new HashMap<>();

		for (String path : smaliClassPathList) {
			List<String> codeLines = new ArrayList<>();
			try {
				/* IO Streams must be closed! Else it won't be able to delete the generated folder.
				 * This step is not necessary with other types of Streams. */
				try (Stream<String> stream = Files.lines(Paths.get(path))) {
//					stream.forEach(line -> codeLines.add(line)); // TODO remove this if new version works
					stream.forEach(codeLines::add);
				}

			} catch (IOException e) {
				LOG.error(e.getMessage());
			}
			String smaliFileName = StringUtils.substringAfterLast(path, File.separator);
			//TODO There might be classes with the same name in different packages for the same activity
			codeMap.put(smaliFileName, codeLines);
		}
		return codeMap;
	}

}
