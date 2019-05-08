package com.greydev.ms_project1.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class Apk {

	private String appName;
	private String packageName;

	// save both as File?
	private String smaliFolderPath;
	private String decodedManifestFilePath;

	private List<Activity> activities = new ArrayList<>();
	private List<BroadcastReceiver> brodcastReceivers = new ArrayList<>();
	private List<ContentProvider> contentProviders = new ArrayList<>();
	private List<Service> services = new ArrayList<>();

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
