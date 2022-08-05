package at.peirleitner.core;

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Nonnull;

import at.peirleitner.core.manager.SettingsManager;
import at.peirleitner.core.system.UserSystem;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.RunMode;
import at.peirleitner.core.util.database.CredentialsFile;
import at.peirleitner.core.util.database.MySQL;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.User;

/**
 * This class represents the Core Instance
 * 
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 */
public final class Core {

	private final RunMode runMode;
	public static Core instance;
	private final MySQL mysql;

	// Manager
	private SettingsManager settingsManager;

	// System
	private UserSystem userSystem;

	/**
	 * Create a new Instance
	 * 
	 * @param runMode - RunMode
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public Core(RunMode runMode) {

		// Initialize
		instance = this;
		this.runMode = runMode;

		// Manager
		this.settingsManager = new SettingsManager();

		// Database
		this.mysql = new MySQL(this.getPluginName(), CredentialsFile.getCredentialsFile(this.getPluginName()));

		if (!mysql.isConnected()) {
			this.log(LogType.CRITICAL, "Could not connect towards MySQL Database, Plugin will not work as intended.");
			return;
		}

		this.createTables();

		// System
		this.userSystem = new UserSystem();

	}

	/**
	 * 
	 * @return Core instance
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final static Core getInstance() {
		return instance;
	}

	/**
	 * 
	 * @return MySQL Instance for the Core
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final MySQL getMySQL() {
		return this.mysql;
	}

	private final void createTables() {

		final Connection connection = this.getMySQL().getConnection();
		final String prefix = this.getMySQL().getTablePrefix();

		try {

			connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + prefix + "players ("
					+ "uuid CHAR(36) NOT NULL, " + "lastKnownName CHAR(16) NOT NULL, "
					+ "registered BIGINT(255) NOT NULL DEFAULT '" + System.currentTimeMillis() + "', "
					+ "lastLogin BIGINT(255) NOT NULL, " + "lastLogout BIGINT(255) NOT NULL, "
					+ "enabled BOOLEAN NOT NULL DEFAULT '1', " + "language VARCHAR(50) NOT NULL DEFAULT '"
					+ this.getDefaultLanguage().toString() + "', " + "PRIMARY KEY (uuid));");

		} catch (SQLException e) {
			this.log(LogType.ERROR, "Could not create MySQL Data Tables: " + e.getMessage());
		}

	}

	/**
	 * Create a log for the Core (using {@link #getPluginName()})
	 * 
	 * @param level   - Level
	 * @param message - Message
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @see #log(String, LogType, String)
	 */
	public final void log(@Nonnull LogType level, @Nonnull String message) {
		this.log(this.getPluginName(), level, message);
	}

	/**
	 * Create a log message
	 * 
	 * @param pluginName - Name of the plugin that issues this log message
	 * @param level      - Level
	 * @param message    - Message
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @see #log(LogType, String)
	 */
	public final void log(@Nonnull String pluginName, @Nonnull LogType level, @Nonnull String message) {

		final String logMessage = "[" + pluginName + "/" + level.toString() + "] " + message;

		if (this.getRunMode() == RunMode.LOCAL) {
			org.bukkit.Bukkit.getConsoleSender().sendMessage(level.getColor() + logMessage);
		} else {
			net.md_5.bungee.api.ProxyServer.getInstance().getConsole()
					.sendMessage(new net.md_5.bungee.api.chat.TextComponent(level.getColor() + logMessage));
		}

	}

	/**
	 * 
	 * @return Current RunMode
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final RunMode getRunMode() {
		return this.runMode;
	}

	/**
	 * 
	 * @return Name of the current main plugin
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @see SpigotMain
	 * @see BungeeMain
	 */
	public final String getPluginName() {
		return this.getRunMode() == RunMode.LOCAL ? SpigotMain.getInstance().getDescription().getName()
				: BungeeMain.getInstance().getDescription().getName();
	}

	/**
	 * 
	 * @return If the admins of this server are performing on a network/proxy.<br>
	 *         This setting is critical as certain data (ex. lastLogin timestamp of
	 *         a user connection) will be updated on every local (ex. spigot)
	 *         connection if this is set to <code>false</code>.
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote If you are running a network, set this to <b>true</b>. If you are
	 *          only running one server instance, set this to <b>false</b>.
	 */
	public final boolean isNetwork() {
		return Boolean
				.valueOf(this.getSettingsManager().getProperties().getProperty("core.manager.settings.is-network"));
	}

	/**
	 * 
	 * @return Default language that will be assigned to a {@link User} upon
	 *         registration
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final Language getDefaultLanguage() {
		return Language.valueOf(Core.getInstance().getSettingsManager().getProperties()
				.getProperty("core.manager.settings.default-language"));
	}

	// | Manager | \\

	/**
	 * 
	 * @return Manager for Settings
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final SettingsManager getSettingsManager() {
		return this.settingsManager;
	}

	// | System | \\

	/**
	 * 
	 * @return System for User interactions
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final UserSystem getUserSystem() {
		return this.userSystem;
	}

}
