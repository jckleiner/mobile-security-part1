package com.greydev.smalitool.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Apk {

	private String name;
	private String packageName;
	private String smaliFolderPath;
	private String decodedManifestFilePath;

	private List<String> permissions = new ArrayList<>();
	// Maps the fully qualified name of the activity class to an Activity instance
	private HashMap<String, Activity> activities = new HashMap<>();
	private HashMap<String, BroadcastReceiver> broadcastReceivers = new HashMap<>();
	private HashMap<String, ContentProvider> contentProviders = new HashMap<>();
	private HashMap<String, Service> services = new HashMap<>();

	@Override
	public String toString() {
		return MessageFormat.format("\nApk Name: {0}\nPackage Name: {1}\nSmali Folder Path: {2}\nDecoded Manifest File Path: {3}\n"
				+ "Permission count: {4}\nActivity count: {5}\nBroadcast Receiver count: {6}\nContent Provider count: {7}\nService count: {8}",
				this.getName(), this.getPackageName(), this.getSmaliFolderPath(), this.getDecodedManifestFilePath(),
				this.getPermissions().size(), this.getActivities().size(), this.getBroadcastReceivers().size(),
				this.getContentProviders().size(), this.getServices().size());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
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

	public HashMap<String, BroadcastReceiver> getBroadcastReceivers() {
		return broadcastReceivers;
	}

	public void setBroadcastReceivers(HashMap<String, BroadcastReceiver> broadcastReceivers) {
		this.broadcastReceivers = broadcastReceivers;
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
