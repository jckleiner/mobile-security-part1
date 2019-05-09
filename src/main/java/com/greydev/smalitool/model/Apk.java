package com.greydev.smalitool.model;

import java.text.MessageFormat;
import java.util.HashMap;

public class Apk {

	private String appName;
	private String packageName;

	// save both as File?
	private String smaliFolderPath;
	private String decodedManifestFilePath;

	private HashMap<String, Activity> activities = new HashMap<>();
	private HashMap<String, BroadcastReceiver> brodcastReceivers = new HashMap<>();
	private HashMap<String, ContentProvider> contentProviders = new HashMap<>();
	private HashMap<String, Service> services = new HashMap<>();

	@Override
	public String toString() {
		return MessageFormat.format("\nApp Name: {0}\nPackage Name: {1}\nSmali Folder Path: {2}\nDecoded Manifest File Path: {3}\n"
				+ "Activity count: {4}\nbrodcastReceiver count: {5}\ncontentProvider count: {6}\nservice count: {7}",
				this.appName, this.packageName, this.smaliFolderPath, this.decodedManifestFilePath,
				this.activities.size(), this.brodcastReceivers.size(), this.contentProviders.size(), this.services.size());
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getSmaliFolderPath() {
		return smaliFolderPath;
	}

	public void setSmaliFolderPath(String smaliFolderPath) {
		this.smaliFolderPath = smaliFolderPath;
	}

	public String getDecodedManifestFilePath() {
		return decodedManifestFilePath;
	}

	public void setDecodedManifestFilePath(String decodedManifestFilePath) {
		this.decodedManifestFilePath = decodedManifestFilePath;
	}

	public HashMap<String, Activity> getActivities() {
		return activities;
	}

	public void setActivities(HashMap<String, Activity> activities) {
		this.activities = activities;
	}

	public HashMap<String, BroadcastReceiver> getBrodcastReceivers() {
		return brodcastReceivers;
	}

	public void setBrodcastReceivers(HashMap<String, BroadcastReceiver> brodcastReceivers) {
		this.brodcastReceivers = brodcastReceivers;
	}

	public HashMap<String, ContentProvider> getContentProviders() {
		return contentProviders;
	}

	public void setContentProviders(HashMap<String, ContentProvider> contentProviders) {
		this.contentProviders = contentProviders;
	}

	public HashMap<String, Service> getServices() {
		return services;
	}

	public void setServices(HashMap<String, Service> services) {
		this.services = services;
	}

}
