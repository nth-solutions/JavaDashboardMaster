package com.bioforceanalytics.dashboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.swing.filechooser.FileSystemView;

import com.bioforceanalytics.dashboard.OSManager.OS;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for storing settings that can be altered from a settings menu in
 * the Advanced Mode Dashboard. Also used by
 * {@link com.bioforceanalytics.dashboard.CSVHandler CSVHandler} in the Data
 * Analysis Graph.
 */
public class Settings {

	private static Properties prop = new Properties();
	private static final Logger logger = LogManager.getLogger();

	static {
		loadConfigFile();
	}

	/**
	 * Reverts configuration file to default settings.
	 * @return whether or not the process completed successfully
	 */
	public static boolean restoreDefaultConfig() {

		Path documentsDir = null;

		if (OSManager.getOS() == OS.WINDOWS) {
			documentsDir = FileSystemView.getFileSystemView().getDefaultDirectory().toPath();
		}
		else if (OSManager.getOS() == OS.MAC) {
			documentsDir = Paths.get(System.getProperty("user.home"), "Documents");
		}

		// OS's "Documents" folder + "BioForce Tests"
		Path saveDir = documentsDir.resolve("BioForce Tests");

		try {
			// create directory if it doesn't exist
			Files.createDirectories(saveDir);
		} catch (IOException e) {
			logger.error("Could not create CSV save location: " + saveDir);
			return false;
		}

		prop.setProperty("CSVSaveLocation", saveDir.toString());
		prop.setProperty("DefaultProfile", "");
		prop.setProperty("TemplateDirectory", "");
		prop.setProperty("OpenOnRead", "False");
		prop.setProperty("AutoSave", "True");

		// save properties to file
		return saveConfig();

	}
	
	/**
	 * Loads the configuration file into a properties object.
	 * @return whether or not the process completed successfully
	 */
	public static boolean loadConfigFile() {	

		try {

			Path settingsPath = Paths.get(System.getProperty("user.home"), ".BioForce Dashboard");

			// create settings if it doesn't exist
			if (!settingsPath.toFile().exists()) {

				logger.warn("Settings directory not found, restoring default configuration...");

				// create settings directory
				Files.createDirectories(settingsPath);

				// generate default config file
				restoreDefaultConfig();

			}

			// load properties file into memory
			prop.load(new FileInputStream(settingsPath.resolve("DataOrganizer.prop").toFile()));

			logger.info("Loaded config file.");
			return true;

		} catch (Exception e) {

			e.printStackTrace();
			logger.error("Error loading config file");
			return false;
			  
		}
	}
	
	/**
	 * Adds or updates a property in the configuration file.
	 * @param key the name of the property
	 * @param val the value of the property
	 */
	public static boolean set(String key, String val) {
		prop.setProperty(key, val);
		return saveConfig();
	}
	
	/**
	 * Saves settings to configuration file.
	 * @return whether or not the process completed successfully
	 */
	public static boolean saveConfig() {

		try {

			Path settingsPath = Paths.get(System.getProperty("user.home"), ".BioForce Dashboard");
			prop.store(new FileOutputStream(settingsPath.resolve("DataOrganizer.prop").toString()), null);

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error saving config file");
			return false;
		}

	}
	
	/**
	 * Retrieves a property from the settings config file.
	 * @param key the name of the property
	 * @return the given settings property
	 */
	public static String get(String key) {

		String value = prop.getProperty(key);

		// create save directory if it doesn't exist
		if (key == "CSVSaveLocation") new File(value).mkdirs();

		return value;

	}
}
