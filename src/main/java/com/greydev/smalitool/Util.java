package com.greydev.smalitool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {

	private static final Logger LOG = Util.getConfiguredLogger(Util.class);

	/**
	 * @param fileName         name of the file to search.
	 * @param workingDirectory base directory for the search.
	 * @return List of all absolute paths find for the keyword, empty list if nothing was found.
	 * @throws FileNotFoundException if the file is not found or is not a directory.
	 * @throws NullPointerException  if fileName or workingDirectory is null.
	 */
	public static List<String> recursiveSearch(String fileName, File workingDirectory)
			throws FileNotFoundException {
		// TODO platform compatibility must be implemented. Commands, file separators etc.

		if (!Objects.requireNonNull(workingDirectory, "Working directory can't be null").isDirectory()) {
			throw new FileNotFoundException("Working directory not found.");
		}

		/* adding \ at the beginning of the class name eliminates files with slightly different names:
		 "WorkingActivity" vs "ServiceWorkingActivity" */
		ProcessBuilder processBuilder = new ProcessBuilder()
				.directory(workingDirectory)
				.command("cmd.exe", "/c", "dir /s /b | findstr \\" + Objects.requireNonNull(fileName));
		List<String> smaliClassPathList = startProcessWithOutputList(processBuilder);

		if (smaliClassPathList == null) {
			return new ArrayList<String>();
		}
		return smaliClassPathList;
	}

	//	Optional<Path> hit = Files.walk(myPath)
	//		   .filter(file -> file.getFileName().equals(myName))
	//		   .findAny();

	private static List<String> startProcessWithOutputList(ProcessBuilder processBuilder) {
		List<String> consoleOutputLines = new ArrayList<>();
		try {
			// allows the error message generated from the process to be sent to input stream
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				consoleOutputLines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return consoleOutputLines;
	}

	public static int startProcess(ProcessBuilder processBuilder) {
		LOG.info("Starting process...");
		int exitCode = 1;
		try {
			processBuilder.redirectErrorStream(true); // allows the error message generated from the process to be sent to input stream
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				LOG.info(line);
			}

			exitCode = process.waitFor(); // returns 0 on success, 1 on failure
			//			LOG.info("\nExited with error code : " + exitCode);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return exitCode;
	}

	public static void deleteFiles(List<String> generatedFolderPaths) {
		LOG.info("Deleting "
				+ Objects.requireNonNull(generatedFolderPaths, "generatedFolderPaths can't be empty").size()
				+ " generated folders...");

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
		LOG.info("Deleted " + generatedFolderPaths.size() + " generated folders successfully.");
	}

	//TODO full path / relative path parsing might be error prone for different platforms
	public static File getFolder(String folderPath) {
		if (!(folderPath.startsWith("/") || folderPath.startsWith("C:\\"))) {
			Path path = FileSystems.getDefault().getPath(".").toAbsolutePath(); // adds a . at the end
			String currentDirectoryPath = StringUtils.removeEnd(path.toString(), ".");
			String userInput = StringUtils.remove(folderPath, "./"); // remove ./ if present
			folderPath = currentDirectoryPath + userInput;
		}
		LOG.info("Searching for folder: " + folderPath + "\n");

		File targetFolder = new File(folderPath);
		if (!targetFolder.isDirectory()) {
			return null;
		}
		return targetFolder;
	}

	public static <T> Logger getConfiguredLogger(Class<T> cls) {
		// System property must be set before initializing the first logger, else it won't read it.
		// the gotcha with setting a system property programatically is that you need to do it early enough; 
		// i.e. before the "logger" code tries to use the property value.
		System.setProperty("smalitool.log.file.path", System.getProperty("user.home") + File.separator + "smalitool.log");
		//		.class is used when there isn't an instance of the class available.
		//		.getClass() is used when there is an instance of the class available.
		return LoggerFactory.getLogger(cls);
	}

}
