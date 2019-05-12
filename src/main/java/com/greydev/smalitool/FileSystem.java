package com.greydev.smalitool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class FileSystem {

	private static final Logger LOG = Utils.getConfiguredLogger(FileSystem.class);

	public static void deleteFiles(List<String> generatedFolderPaths) {
		Objects.requireNonNull(generatedFolderPaths, "generatedFolderPaths can't be empty");
		LOG.info("Deleting {} generated folders...", generatedFolderPaths.size());

		generatedFolderPaths.forEach(folderPath -> {
			File currentFolder = new File(folderPath);
			if (currentFolder.isDirectory()) {
				try {
					FileUtils.deleteDirectory(currentFolder);
				} catch (IOException e) {
					LOG.debug(e.getMessage());
				}
			}
		});
		LOG.info("Deleted {} generated folders successfully.", generatedFolderPaths.size());
	}

	//TODO full path / relative path parsing might be error prone for different platforms
	public static File getFolder(String folderPath) {
		if (!(folderPath.startsWith("/") || folderPath.startsWith("C:\\"))) {
			Path path = FileSystems.getDefault().getPath(".").toAbsolutePath(); // adds a . at the end
			LOG.info("FileSystems.getDefault().getPath(\".\").toAbsolutePath(): {}", path);
			String currentDirectoryPath = StringUtils.removeEnd(path.toString(), ".");
			folderPath = StringUtils.removeStart(folderPath, "./"); // remove ./ if present (unix current dir)
			folderPath = StringUtils.removeStart(folderPath, ".\\"); // remove .\ if present (windows current dir)
			if (StringUtils.equals(folderPath, ".")) {
				folderPath = "";
			}
			folderPath = currentDirectoryPath + folderPath;
		}
		LOG.info("Searching for folder: {}\n", folderPath);

		File targetFolder = new File(folderPath);
		if (!targetFolder.isDirectory()) {
			return null;
		}
		return targetFolder;
	}

	public static List<String> recursiveSearch(String fileName, File workingDirectory) throws FileNotFoundException {
		Objects.requireNonNull(workingDirectory, "workingDirectory can't be null");
		Objects.requireNonNull(fileName, "fileName can't be null");

		if (!workingDirectory.isDirectory()) {
			throw new FileNotFoundException("Working directory not found under " + workingDirectory.getAbsolutePath());
		}
		List<String> pathList = new ArrayList<>();
		try {
			Files.walk(Paths.get(workingDirectory.getAbsolutePath()))
					.filter(Files::isRegularFile)
					.forEach(path -> {
						String filePath = path.toString();
						if (filePath.contains(File.separator + fileName)) {
							pathList.add(filePath);
						}
					});
		} catch (IOException e) {
			LOG.debug(e.getMessage());
		}
		return pathList;
	}

	/**
	 * @deprecated does the same thing as recursiveSearch method.
	 * @param fileName         name of the file to search.
	 * @param workingDirectory base directory for the search.
	 * @return List of all absolute paths find for the keyword, empty list if nothing was found.
	 * @throws FileNotFoundException if the file is not found or is not a directory.
	 * @throws NullPointerException  if fileName or workingDirectory is null.
	 */
	@Deprecated
	public static List<String> recursiveSearchViaCLI(String fileName, File workingDirectory)
			throws FileNotFoundException {
		Objects.requireNonNull(workingDirectory, "workingDirectory can't be null");
		Objects.requireNonNull(fileName, "fileName can't be null");

		if (!workingDirectory.isDirectory()) {
			throw new FileNotFoundException("Working directory not found under " + workingDirectory.getAbsolutePath());
		}
		// TODO platform compatibility must be implemented. Commands, file separators etc.
		ProcessBuilder processBuilder = new ProcessBuilder().directory(workingDirectory);

		/* adding \ at the beginning of the class name eliminates files with slightly different names:
		 "Worker" vs "ListenableWorker" */
		if (Utils.isOsWindows()) {
			processBuilder.command("cmd.exe", "/c", "dir /s /b | findstr " + File.separator + fileName);
		}
		else if (Utils.isOsUnixBased()) {
			processBuilder.command("/bin/bash", "-c", "find $PWD | grep " + File.separator + fileName);
		}
		else {
			LOG.error("Operating system can't be detected");
		}
		List<String> smaliClassPathList = Utils.startProcessWithOutputList(processBuilder);

		if (smaliClassPathList == null) {
			return new ArrayList<String>();
		}
		return smaliClassPathList;
	}

}
