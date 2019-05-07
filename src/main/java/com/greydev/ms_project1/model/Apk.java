package com.greydev.ms_project1.model;

import java.util.List;

public class Apk {

	private String appName;
	private String packageName;

	// save both as File?
	private String smaliFolderPath;
	private String decodedManifestFilePath;

	private List<Activity> activities;
	private List<BroadcastReceiver> brodcastReceivers;
	private List<ContentProvider> contentProviders;
	private List<Service> services;

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

	public List<Activity> getActivities() {
		return activities;
	}

	public void setActivities(List<Activity> activities) {
		this.activities = activities;
	}

	public List<BroadcastReceiver> getBrodcastReceivers() {
		return brodcastReceivers;
	}

	public void setBrodcastReceivers(List<BroadcastReceiver> brodcastReceivers) {
		this.brodcastReceivers = brodcastReceivers;
	}

	public List<ContentProvider> getContentProviders() {
		return contentProviders;
	}

	public void setContentProviders(List<ContentProvider> contentProviders) {
		this.contentProviders = contentProviders;
	}

	public List<Service> getServices() {
		return services;
	}

	public void setServices(List<Service> services) {
		this.services = services;
	}

}
