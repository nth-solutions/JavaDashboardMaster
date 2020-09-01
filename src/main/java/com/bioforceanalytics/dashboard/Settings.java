package com.bioforceanalytics.dashboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.swing.filechooser.FileSystemView;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for storing settings that can be altered from a settings menu in the Advanced Mode Dashboard.
 * Also used by {@link com.bioforceanalytics.dashboard.CSVHandler CSVHandler} in the Data Analysis Graph.
 */
public class Settings {

	Properties prop = new Properties();

	private static final Logger logger = LogManager.getLogger();

	//Defines the default configurations
	public void restoreDefaultConfig() {
		
		String path = FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "/BioForce Tests/";
		
		this.prop.setProperty("CSVSaveLocation", path);
		this.prop.setProperty("DefaultProfile", "");
		this.prop.setProperty("TemplateDirectory", "");
		this.prop.setProperty("OpenOnRead", "False");
		this.prop.setProperty("AutoSave", "True");
		this.saveConfig();

	}
	
	//Loads saved configurations from DataOrganizer.prop
	public void loadConfigFile() {	

		try {

			File SettingsDirectory = new File(System.getProperty("user.home")+"/.BioForce Dashboard/");

			if (!SettingsDirectory.exists()) SettingsDirectory.mkdirs();

			this.prop.load(new FileInputStream(SettingsDirectory + "/DataOrganizer.prop"));

		} catch (FileNotFoundException e) {

	  		logger.warn("Config file could not be found, reverting to default config...");
			this.restoreDefaultConfig();

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error loading config file");
			  
		}
	}
	
	//Sets the key value and saves
	public void setProp(String key, String val) {
		this.prop.setProperty(key, val);
		this.saveConfig();
	}
	
	//saves configuration to DataOrganizer.prop file
	public void saveConfig() {

		try {

			File SettingsDirectory = new File(System.getProperty("user.home") + "/.BioForce Dashboard/");

	  		if (!SettingsDirectory.exists()) {
				SettingsDirectory.mkdirs();
	  		}
			
			this.prop.store(new FileOutputStream(SettingsDirectory + "/DataOrganizer.prop"), null);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error saving config file");
		}

	}
	
	//returns the key value
	public String getKeyVal(String Key) {
		return this.prop.getProperty(Key);
	}
}
