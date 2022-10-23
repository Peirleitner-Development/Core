package at.peirleitner.core.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import at.peirleitner.core.Core;
import at.peirleitner.core.command.local.CommandSlot;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.PredefinedDatabaseSetting;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.database.TableType;
import at.peirleitner.core.util.user.CorePermission;
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
		this.setDefaultValues(Core.getInstance().getPluginName());
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

		// v1.0.0
		map.put(path + "is-network", "false");
		map.put(path + "default-language", Language.ENGLISH.toString());
		map.put(path + "log-with-simple-class-names", "true");
		map.put(path + "maintenance", "false");
		map.put(path + "server-name", "Example.com");
		map.put(path + "chat.use-core-chat-format", "true");
		map.put(path + "chat.chat-format", "{player}&8: {message}");
		map.put(path + "saveType", "-1");
		map.put(path + "use-tab-header", "true");

		// v1.0.3
		map.put(path + "cache-game-maps", "false");

		// v1.0.4
		map.put(path + "cache-motd", "true");

		// v1.0.5
		map.put(path + "disable-motd-server-list-ping", "false");
		map.put(path + "disable-leaves-decay", "false");
		map.put(path + "operator-join-action", "ALLOW");
		map.put(path + "server-website", "www.example.com");
		
		// v1.0.6
		map.put(path + "chat.enable-mention-pings", "true");
		map.put(path + "slots", "50");
		map.put(path + "server-store", "store.example.com");

		return map;
	}
	
	/**
	 * 
	 * @return Maximum slots of the server
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 * @see CommandSlot
	 * @see CorePermission#BYPASS_FULL_SERVER_JOIN
	 */
	public final int getSlots() {
		return Integer.valueOf(this.getSetting(Core.getInstance().getPluginName(), "manager.settings.slots"));
	}
	
	/**
	 * 
	 * @param amount - New Slots
	 * @return If the setting has been changed successfully
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 * @see #setSetting(String, String, String)
	 */
	public final boolean setSlots(@Nonnull int amount) {
		return this.setSetting(Core.getInstance().getPluginName(), "manager.settings.slots", "" + amount);
	}

	public final String getServerName() {
		return this.getSetting(Core.getInstance().getPluginName(), PredefinedMessage.SERVER_NAME.getPath());
	}
	
	/**
	 * 
	 * @return Website
	 * @since 1.0.5
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getServerWebsite() {
		return this.getSetting(Core.getInstance().getPluginName(), PredefinedMessage.SERVER_WEBSITE.getPath());
	}
	
	/**
	 * 
	 * @return Website of the Store
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getServerStore() {
		return this.getSetting(Core.getInstance().getPluginName(), PredefinedMessage.SERVER_STORE.getPath());
	}

	public final boolean isChatFormatEnabled() {
		return Boolean.valueOf(
				this.getSetting(Core.getInstance().getPluginName(), "manager.settings.chat.use-core-chat-format"));
	}

	public final String getChatFormat() {
		return this.getSetting(Core.getInstance().getPluginName(), "manager.settings.chat.chat-format");
	}

	public final SaveType getSaveType() {
		return Core.getInstance().getSaveTypeByID(
				Integer.valueOf(this.getSetting(Core.getInstance().getPluginName(), "manager.settings.saveType")));
	}

	public final boolean isUseTabHeader() {
		return Boolean.valueOf(this.getSetting(Core.getInstance().getPluginName(), "manager.settings.use-tab-header"));
	}

	private final void setDefaultValues(@Nonnull String pluginName) {

		// Only set default values for the Core Instance
		if (!pluginName.equals(Core.getInstance().getPluginName()))
			return;
		for (Map.Entry<String, String> entry : this.getDefaultValues().entrySet()) {
			this.registerSetting(pluginName, entry.getKey(), entry.getValue());
		}

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
	 * Register a new setting. This will only use
	 * {@link #setSetting(String, String, String)} if the setting doesn't exist.
	 * 
	 * @param pluginName - Name of the Plugin
	 * @param key        - Unique key
	 * @param value      - Value
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
	
	/**
	 * 
	 * @return
	 * @since 1.0.5
	 */
	public final boolean setDefaultDatabaseSettings() {
		
		if(Core.getInstance().getMySQL() == null || !Core.getInstance().getMySQL().isConnected()) {
			Core.getInstance().log(getClass(), LogType.DEBUG, "Did not attempt to load default database settings because mysql connection has not been established.");
			return false;
		}
		
		try {
			
			for(PredefinedDatabaseSetting pds : PredefinedDatabaseSetting.values()) {
				
				PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("INSERT IGNORE INTO " + TableType.SETTINGS.getTableName(true) + " (setting, value, staff, changed) VALUES (?, ?, ?, ?);");
				stmt.setString(1, pds.name().toString().toLowerCase());
				stmt.setString(2, pds.getDefaultValue());
				stmt.setString(3, null);
				stmt.setLong(4, System.currentTimeMillis());
				
				stmt.executeUpdate();
				
			}
			
			return true;
			
		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not set default database settings/SQL: " + e.getMessage());
			return false;
		}
		
	}
	
	/**
	 * 
	 * @return
	 * @since 1.0.5
	 */
	public final boolean setDatabaseSetting(@Nonnull String key, @Nonnull String value, @Nullable UUID staff) {
		
		try {
			
			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("UPDATE " + TableType.SETTINGS.getTableName(true) + " SET value = ?, staff = ?, changed = ? WHERE key = ?");
			stmt.setString(1, value);
			stmt.setString(2, staff == null ? null : staff.toString());
			stmt.setLong(3, System.currentTimeMillis());
			stmt.setString(4, key);
			
			stmt.executeUpdate();
			return true;
			
		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not set database setting '" + key + "'/SQL: " + e.getMessage());
			return false;
		}
		
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 * @since 1.0.5
	 */
	public final String getDatabaseSetting(@Nonnull String key) {
		
		//TODO: Return as 'DatabaseSetting' object
		
		try {
			
			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("SELECT value FROM " + TableType.SETTINGS.getTableName(true) + " WHERE key = ?");
			ResultSet rs = stmt.executeQuery();
			
			if(rs.next()) {
				return rs.getString(1);
			} else {
				// No result
				return null;
			}
			
		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not get Database Setting '" + key + "/SQL:" + e.getMessage());
			return null;
		}
		
	}

}
