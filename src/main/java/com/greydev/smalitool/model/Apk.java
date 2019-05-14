package com.greydev.smalitool.model;

import java.text.MessageFormat;
import java.util.HashMap;

public class Apk {

	private String packageName;

	// save both as File?
	private String smaliFolderPath;
	private String decodedManifestFilePath;

	// TODO add permissions

	private HashMap<String, Activity> activities = new HashMap<>();
	private HashMap<String, BroadcastReceiver> brodcastReceivers = new HashMap<>();
	private HashMap<String, ContentProvider> contentProviders = new HashMap<>();
	private HashMap<String, Service> services = new HashMap<>();

	@Override
	public String toString() {
		return MessageFormat.format("\nPackage Name: {0}\nSmali Folder Path: {1}\nDecoded Manifest File Path: {2}\n"
				+ "Activity count: {3}\nbrodcastReceiver count: {4}\ncontentProvider count: {5}\nservice count: {6}",
				this.packageName, this.smaliFolderPath, this.decodedManifestFilePath,
				this.activities.size(), this.brodcastReceivers.size(), this.contentProviders.size(), this.services.size());
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
