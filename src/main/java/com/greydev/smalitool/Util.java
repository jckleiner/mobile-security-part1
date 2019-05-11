package com.greydev.smalitool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {

	private static final Logger LOG = Util.getConfiguredLogger(Util.class);
	private static final OS OPERATING_SYSTEM = Util.detectOs();

	public static List<String> startProcessWithOutputList(ProcessBuilder processBuilder) {
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

	public static <T> Logger getConfiguredLogger(Class<T> cls) {
		// System property must be set before initializing the first logger, else it won't read it.
		// the gotcha with setting a system property programatically is that you need to do it early enough; 
		// i.e. before the "logger" code tries to use the property value.
		System.setProperty("smalitool.log.file.path", System.getProperty("user.home") + File.separator + "smalitool.log");
		//		.class is used when there isn't an instance of the class available.
		//		.getClass() is used when there is an instance of the class available.
		return LoggerFactory.getLogger(cls);
	}

	public static boolean isOsWindows() {
		return Util.OPERATING_SYSTEM == OS.WINDOWS;
	}

	public static boolean isOsUnixBased() {
		return (Util.OPERATING_SYSTEM == OS.MAC
				|| Util.OPERATING_SYSTEM == OS.UNIX
				|| Util.OPERATING_SYSTEM == OS.POSIX_UNIX);
	}

	public static OS detectOs() {
		String osName = System.getProperty("os.name");
		LOG.debug("Detected OS: {}", osName);
		Objects.requireNonNull(osName);
		OS os = OS.OTHER;
		osName = osName.toLowerCase(Locale.ENGLISH);
		if (osName.contains("windows")) {
			os = OS.WINDOWS;
		}
		else if (osName.contains("linux")
				|| osName.contains("mpe/ix")
				|| osName.contains("freebsd")
				|| osName.contains("irix")
				|| osName.contains("digital unix")
				|| osName.contains("unix")) {
			os = OS.UNIX;
		}
		else if (osName.contains("mac")) {
			os = OS.MAC;
		}
		else if (osName.contains("sun os")
				|| osName.contains("sunos")
				|| osName.contains("solaris")
				|| osName.contains("hp-ux")
				|| osName.contains("aix")) {
			os = OS.POSIX_UNIX;
		}
		else {
			os = OS.OTHER;
		}
		return os;
	}

	public enum OS {
		WINDOWS,
		UNIX,
		POSIX_UNIX,
		MAC,
		OTHER;
	}
}
