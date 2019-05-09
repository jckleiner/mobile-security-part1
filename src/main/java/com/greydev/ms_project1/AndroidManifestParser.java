package com.greydev.ms_project1;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class AndroidManifestParser {

	private static final String XPATH_APPLICATION = "//manifest/application";
	private static final String XPATH_ACTIVITIES = XPATH_APPLICATION + "/activity";
	private static final String XPATH_RECEIVERS = XPATH_APPLICATION + "/receiver";
	private static final String XPATH_PROVIDERS = XPATH_APPLICATION + "/provider";
	private static final String XPATH_SERVICES = XPATH_APPLICATION + "/service";
	private static final String XPATH_ACTIONS = "intent-filter/action";
	private static final String XPATH_ANDROID_NAME = "@android:name";
	private static final String XPATH_PACKAGE = "@package";

	private File manifestFile;
	private Document document;

	public AndroidManifestParser(String manifestFilePath) throws FileNotFoundException, DocumentException {
		this.manifestFile = new File(manifestFilePath);
		if (!this.manifestFile.isFile()) {
			throw new FileNotFoundException("Android manifest file is not found.");
		}
		this.document = new SAXReader().read(manifestFile); // a URL object can also be passed as an argument
	}

	public File getManifestFile() {
		return manifestFile;
	}

	public String getPackageName() {
		Element root = document.getRootElement(); // <manifest> element
		return root.valueOf(XPATH_PACKAGE);
	}

	public List<Node> getActivities() {
		return document.selectNodes(XPATH_ACTIVITIES);
	}

	public List<Node> getBroadcastReceivers() {
		return document.selectNodes(XPATH_RECEIVERS);
	}

	public List<Node> getServices() {
		return document.selectNodes(XPATH_SERVICES);
	}

	public List<Node> getContentProviders() {
		return document.selectNodes(XPATH_PROVIDERS);
	}

	public String getNodeName(Node node) {
		return node.valueOf(XPATH_ANDROID_NAME);
	}

	public List<String> getIntentActions(Node node) {
		// '//intent-filter/action' returns all nodes inside the xml file independent of the current node
		// 'intent-filter/action' returns only the child intent-filer/actions for the current node
		List<String> intentList = new ArrayList<>();
		node.selectNodes(XPATH_ACTIONS).forEach(action -> {
			String intent = action.valueOf(XPATH_ANDROID_NAME);
			intentList.add(intent);
		});
		return intentList;
	}

}