package at.peirleitner.core.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.PredefinedMessage;

/**
 * Class to manage settings
 * 
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 */
public class SettingsManager {

	public SettingsManager() {
		this.createSettingsDirectory();
		this.createProperties(Core.getInstance().getPluginName());
	}

	private final File getSettingsDirectory() {
		return new File(this.getDataFolder() + "/settings");
	}

	private final void createSettingsDirectory() {
		if (!this.getSettingsDirectory().exists()) {
			this.getSettingsDirectory().mkdir();
		}
	}

	private final void createProperties(@Nonnull String pluginName) {

		File f = this.getFile(pluginName);

		if (!f.exists()) {

			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Could not find file '" + f.getName() + "', attempting to create..");

			try {

				f.createNewFile();
				Core.getInstance().log(this.getClass(), LogType.DEBUG, "Successfully created a new Settings file.");

				this.setDefaultValues(pluginName);

			} catch (IOException e) {
				Core.getInstance().log(this.getClass(), LogType.ERROR,
						"Could not create new Settings file: " + e.getMessage());
			}
		} else {
//			Core.getInstance().log(this.getClass(), LogType.DEBUG,
//					"Did not attempt to create a new Settings file because one does already exist.");
		}

	}

	private final HashMap<String, String> getDefaultValues() {

		final HashMap<String, String> map = new HashMap<>();
		final String path = "manager.settings.";

		map.put(path + "is-network", "false");
		map.put(path + "default-language", Language.ENGLISH.toString());
		map.put(path + "log-with-simple-class-names", "true");
		map.put(path + "maintenance", "false");
		map.put(path + "server-name", "Example.com");
		map.put(path + "chat.use-core-chat-format", "true");
		map.put(path + "chat.chat-format", "{player}&8: {message}");
		map.put(path + "saveType", "-1");
		map.put(path + "use-tab-header", "true");

		return map;
	}

	public final String getServerName() {
		return this.getSetting(Core.getInstance().getPluginName(), PredefinedMessage.SERVER_NAME.getPath());
	}

	public final boolean isChatFormatEnabled() {
		return Boolean.valueOf(this.getSetting(Core.getInstance().getPluginName(), "manager.settings.chat.use-core-chat-format"));
	}

	public final String getChatFormat() {
		return this.getSetting(Core.getInstance().getPluginName(), "manager.settings.chat.chat-format");
	}

	public final SaveType getSaveType() {
		return Core.getInstance().getSaveTypeByID(Integer.valueOf(this.getSetting(Core.getInstance().getPluginName(), "manager.settings.saveType")));
	}

	public final boolean isUseTabHeader() {
		return Boolean.valueOf(this.getSetting(Core.getInstance().getPluginName(), "manager.settings.use-tab-header"));
	}

	private final void setDefaultValues(@Nonnull String pluginName) {

		if (this.getFile(pluginName) == null || !this.getFile(pluginName).exists()) {
			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Did not attempt to set default values because settings file does not exist.");
			return;
		}

		// Only set default values for the Core Instance
		if (!pluginName.equals(Core.getInstance().getPluginName()))
			return;

		Properties p = this.getProperties(pluginName);

		for (Map.Entry<String, String> entry : this.getDefaultValues().entrySet()) {

			if (this.getProperties(pluginName).get(entry.getKey()) == null) {
				p.setProperty(entry.getKey(), entry.getValue());
				Core.getInstance().log(this.getClass(), LogType.DEBUG,
						"Settings: Added default key '" + entry.getKey() + "' with value '" + entry.getValue() + "'.");
			}

		}

		this.save(pluginName, p);

	}

	private final boolean save(String pluginName, Properties p) {

		try {
			p.store(new FileWriter(this.getFile(pluginName)), "Last update on " + new Date(System.currentTimeMillis()));
			return true;
		} catch (IOException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not save Settings file: " + e.getMessage());
			return false;
		}

	}

	private final File getDataFolder() {
		return Core.getInstance().getDataFolder();
	}

	public final File getFile(@Nonnull String pluginName) {
		return new File(this.getSettingsDirectory() + "/" + pluginName + ".properties");
	}

	private final Properties getProperties(@Nonnull String pluginName) {
		
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(this.getFile(pluginName)));
		} catch (FileNotFoundException e) {
			this.createProperties(pluginName);
			return this.getProperties(pluginName);
		} catch (IOException e) {
			Core.getInstance().log(this.getClass(), LogType.WARNING,
					"Could not get settings file for plugin '" + pluginName + "': " + e.getMessage());
			return null;
		}
		return p;
	}

	/**
	 * Register a new setting. This will only use {@link #setSetting(String, String, String)} if the setting doesn't exist.
	 * @param pluginName - Name of the Plugin
	 * @param key - Unique key
	 * @param value - Value
	 * @return If the setting has been registered
	 * @since 1.0.2
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean registerSetting(@Nonnull String pluginName, @Nonnull String key, @Nonnull String value) {
		return this.getSetting(pluginName, key) == null ? this.setSetting(pluginName, key, value) : false;
	}
	
	public final boolean setSetting(@Nonnull String pluginName, @Nonnull String key, @Nonnull String value) {
		Properties p = this.getProperties(pluginName);
		p.setProperty(key, value);
		return this.save(pluginName, p);
	}

	public final String getSetting(@Nonnull String pluginName, @Nonnull String key) {
		return this.getProperties(pluginName).getProperty(key);
	}

	public final boolean isSetting(@Nonnull String pluginName, @Nonnull String key) {
		return Boolean.valueOf(this.getSetting(pluginName, key));
	}

	public final boolean removeSetting(@Nonnull String pluginName, @Nonnull String key) {
		Properties p = this.getProperties(pluginName);
		p.remove(key);
		return this.save(pluginName, p);
	}

}
