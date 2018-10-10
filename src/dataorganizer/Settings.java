package dataorganizer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.filechooser.FileSystemView;

public class Settings {
	Properties prop = new Properties();			//Defines properties
	
	
	//Defines the default configurations
	public void restoreDefaultConfig() {			
		this.prop.setProperty("CSVSaveLocation", FileSystemView.getFileSystemView().getDefaultDirectory().getPath());
		this.prop.setProperty("DefaultProfile", "");
		this.prop.setProperty("TemplateDirectory", "");
		this.prop.setProperty("OpenOnRead", "False");
		this.prop.setProperty("AutoSave", "True");
		this.saveConfig();
	}
	
	//Loads saved configurations from DataOrganizer.prop
	public void loadConfigFile(){				
		try{
			this.prop.load(new FileInputStream("DataOrganizer.prop"));
		}catch(FileNotFoundException e) {
			this.restoreDefaultConfig();
		}catch(IOException e) {
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
			this.prop.store(new FileOutputStream("DataOrganizer.prop"), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//returns the key value
	public String getKeyVal(String Key) {
		return this.prop.getProperty(Key);
	}
}
