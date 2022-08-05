package at.peirleitner.core.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import at.peirleitner.core.BungeeMain;
import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.RunMode;
import at.peirleitner.core.util.user.Language;

/**
 * Class to manage settings
 * 
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 */
public class SettingsManager {

	private Properties properties;
	private boolean initialized = false;

	//TODO: Core/settings/<Plugin>.properties - Save to file with pluginName
	public SettingsManager() {
		
		this.createProperties();
		this.loadProperties();

	}

	private final void createProperties() {

		File f = this.getFile();

		if (!f.exists()) {

			Core.getInstance().log(LogType.DEBUG, "Could not find file '" + f.getName() + "', attempting to create..");

			try {
				
				f.createNewFile();
				Core.getInstance().log(LogType.DEBUG, "Successfully created a new Settings file.");
				
				this.loadProperties();
				
			} catch (IOException e) {
				Core.getInstance().log(LogType.ERROR, "Could not create new Settings file: " + e.getMessage());
			}
		} else {
			Core.getInstance().log(LogType.DEBUG,
					"Did not attempt to create a new Settings file because one does already exist.");
		}

	}

	private final void loadProperties() {

		if (this.getFile() == null || !this.getFile().exists()) {
			Core.getInstance().log(LogType.DEBUG, "Did not attempt to load settings file because none does exist.");
			return;
		}
		
		if(this.initialized) {
			Core.getInstance().log(LogType.DEBUG, "Did not attempt to load settings file because it has already been initialized.");
			return;
		}
		
		try {
			
			this.properties = new Properties();
			this.properties.load(new FileInputStream(this.getFile()));
			
			this.setDefaultValues();
			this.initialized = true;
			
		} catch (IOException e) {
			Core.getInstance().log(LogType.ERROR, "Error while attempting to load Settings file: " + e.getMessage());
		}

	}
	
	private final HashMap<String, String> getDefaultValues() {
		
		final HashMap<String, String> map = new HashMap<>();
		final String path = "core.manager.settings.";
		
		map.put(path + "is-network", "false");
		map.put(path + "default-language", Language.ENGLISH.toString());
		
		return map;
	}
	
	private final void setDefaultValues() {
		
		if (this.getFile() == null || !this.getFile().exists()) {
			Core.getInstance().log(LogType.DEBUG, "Did not attempt to set default values because settings file does not exist.");
			return;
		}
		
		for(Map.Entry<String, String> entry : this.getDefaultValues().entrySet()) {
			
			if(this.getProperties().get(entry.getKey()) == null) {
				this.getProperties().setProperty(entry.getKey(), entry.getValue());
				Core.getInstance().log(LogType.DEBUG, "Settings: Added default key '" + entry.getKey() + "' with value '" + entry.getValue() + "'.");
			}
			
		}
		
		this.save();
		
	}
	
	public final boolean save() {
		
		try {
			this.getProperties().store(new FileWriter(this.getFile()), "Last update on " + new Date(System.currentTimeMillis()));
			return true;
		} catch (IOException e) {
			Core.getInstance().log(LogType.ERROR, "Could not save Settings file: " + e.getMessage());
			return false;
		}
		
	}

	private final File getDataFolder() {
		return Core.getInstance().getRunMode() == RunMode.NETWORK ? BungeeMain.getInstance().getDataFolder()
				: SpigotMain.getInstance().getDataFolder();
	}

	private final File getFile() {

		File f = new File(this.getDataFolder() + "/settings.properties");

		return f;

	}

	public final Properties getProperties() {
		return this.properties;
	}

}
