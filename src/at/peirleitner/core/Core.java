package at.peirleitner.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import at.peirleitner.core.manager.LanguageManager;
import at.peirleitner.core.manager.SettingsManager;
import at.peirleitner.core.system.EconomySystem;
import at.peirleitner.core.system.GameMapSystem;
import at.peirleitner.core.system.LicenseSystem;
import at.peirleitner.core.system.MaintenanceSystem;
import at.peirleitner.core.system.ModerationSystem;
import at.peirleitner.core.system.MotdSystem;
import at.peirleitner.core.system.StatSystem;
import at.peirleitner.core.system.UserSystem;
import at.peirleitner.core.util.DiscordWebHookType;
import at.peirleitner.core.util.DiscordWebhook;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.RunMode;
import at.peirleitner.core.util.database.CredentialsFile;
import at.peirleitner.core.util.database.MySQL;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.database.SaveType.WorldType;
import at.peirleitner.core.util.database.TableType;
import at.peirleitner.core.util.local.RankType;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.LanguagePhrase;
import at.peirleitner.core.util.user.Rank;
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
	private final Gson gson;
	private Collection<SaveType> saveTypes;
	private final Collection<Rank> ranks;
	private File ranksFile;

	private final String DISCORD_WEBHOOK_START_URL = "https://discord.com/api/webhooks/";
	private final String DISCORD_WEBHOOK_INVALID = "Webhook URL invalid";
	private final String DISCORD_WEBHOOK_ERROR = "Error on Webhook execution: {error}";

	// Manager
	private SettingsManager settingsManager;
	private LanguageManager languageManager;

	// System
	private UserSystem userSystem;
	private StatSystem statSystem;
	private GameMapSystem gameMapSystem;
	private MotdSystem motdSystem;
	private MaintenanceSystem maintenanceSystem;
	private LicenseSystem licenseSystem;
	private EconomySystem economySystem;
	private ModerationSystem moderationSystem;

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
		this.gson = new GsonBuilder().setPrettyPrinting().create();
		this.saveTypes = new ArrayList<>();
		this.ranks = new ArrayList<>();
		this.ranksFile = new File(this.getDataFolder().getPath() + "/ranks.json");
		this.loadRanks();

		// Manager
		this.settingsManager = new SettingsManager();
		this.languageManager = new LanguageManager();

		this.registerMessages();

		// Database
		this.mysql = new MySQL(this.getPluginName(),
				CredentialsFile.getCredentialsFile(this.getPluginName(), this.getDataFolder().getPath()));

		if (!mysql.isConnected()) {
			this.log(this.getClass(), LogType.CRITICAL,
					"Could not connect towards MySQL Database, Plugin will not work as intended.");
			return;
		}

		this.createTables();
		this.loadSaveTypes();
//		this.settingsManager.setDefaultDatabaseSettings();

		// System
		this.userSystem = new UserSystem();
		this.statSystem = new StatSystem();
		this.gameMapSystem = new GameMapSystem();
		this.motdSystem = new MotdSystem();
		this.maintenanceSystem = new MaintenanceSystem();
		this.licenseSystem = new LicenseSystem();
		this.economySystem = new EconomySystem();
		this.moderationSystem = new ModerationSystem();

		this.log(this.getClass(), LogType.DEBUG, "Successfully enabled the Core instance with RunMode " + runMode
				+ ". Network-Mode is set to " + this.isNetwork() + ".");

		// Checks
		if (this.getSettingsManager().getSaveType() == null) {
			this.log(this.getClass(), LogType.WARNING,
					"SaveType has not been set inside '"
							+ this.getSettingsManager().getFile(this.getPluginName()).getPath()
							+ "', database interaction will not work on some systems until this has been set.");
		} else {
			this.log(this.getClass(), LogType.DEBUG,
					"Running on SaveType " + this.getSettingsManager().getSaveType().getName() + ".");
		}

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
	 * @return Gson instance
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final Gson getGson() {
		return this.gson;
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

	/**
	 * @deprecated See {@link TableType}
	 * @return {@link #table_saveType}
	 * @since 1.0.2
	 * @author Markus Peirleitner (Rengobli)
	 */
	@Deprecated(forRemoval = true, since = "1.0.6")
	public final String getTableSaveType() {
		return TableType.SAVE_TYPE.getTableName(true);
	}

	/**
	 * @deprecated See {@link TableType}
	 * @return {@link #table_users}
	 * @since 1.0.2
	 * @author Markus Peirleitner (Rengobli)
	 */
	@Deprecated(forRemoval = true, since = "1.0.6")
	public final String getTableUsers() {
		return TableType.USERS.getTableName(true);
	}

	/**
	 * @deprecated See {@link TableType}
	 * @return {@link #table_stats}
	 * @since 1.0.2
	 * @author Markus Peirleitner (Rengobli)
	 */
	@Deprecated(forRemoval = true, since = "1.0.6")
	public final String getTableStats() {
		return TableType.STATS.getTableName(true);
	}

	/**
	 * @deprecated See {@link TableType}
	 * @return {@link #table_shop}
	 * @since 1.0.2
	 * @author Markus Peirleitner (Rengobli)
	 */
	@Deprecated(forRemoval = true, since = "1.0.6")
	public final String getTableShop() {
		return TableType.SHOP.getTableName(true);
	}

	/**
	 * @deprecated See {@link TableType}
	 * @return {@link #table_maps}
	 * @since 1.0.2
	 * @author Markus Peirleitner (Rengobli)
	 */
	@Deprecated(forRemoval = true, since = "1.0.6")
	public final String getTableMaps() {
		return TableType.MAPS.getTableName(true);
	}

	/**
	 * @deprecated See {@link TableType}
	 * @return {@link #table_motd}
	 * @since 1.0.4
	 * @author Markus Peirleitner (Rengobli)
	 */
	@Deprecated(forRemoval = true, since = "1.0.6")
	public final String getTableMotd() {
		return TableType.MOTD.getTableName(true);
	}

	/**
	 * @deprecated See {@link TableType}
	 * @return {@link #table_settings}
	 * @since 1.0.5
	 * @author Markus Peirleitner (Rengobli)
	 */
	@Deprecated(forRemoval = true, since = "1.0.6")
	public final String getTableSettings() {
		return TableType.SETTINGS.getTableName(true);
	}

	/**
	 * @deprecated See {@link TableType}
	 * @return {@link #table_maintenance}
	 * @since 1.0.5
	 * @author Markus Peirleitner (Rengobli)
	 */
	@Deprecated(forRemoval = true, since = "1.0.6")
	public final String getTableMaintenance() {
		return TableType.MAINTENANCE.getTableName(true);
	}

	public final File getDataFolder() {
		return this.getRunMode() == RunMode.LOCAL ? SpigotMain.getInstance().getDataFolder()
				: BungeeMain.getInstance().getDataFolder();
	}

	public Collection<SaveType> getSaveTypes() {
		return this.saveTypes;
	}

	public SaveType getSaveTypeByName(@Nonnull String name) {
		return this.saveTypes.stream().filter(st -> st.getName().equalsIgnoreCase(name)).findAny().orElse(null);
	}

	public SaveType getSaveTypeByID(@Nonnull int id) {
		return this.saveTypes.stream().filter(st -> st.getID() == id).findAny().orElse(null);
	}

	@SuppressWarnings("serial")
	private final void loadRanks() {

		// Create
		if (!this.ranksFile.exists()) {
			try {

				// Create File
				this.ranksFile.createNewFile();

				// Fill with default values
				List<Rank> defaultValues = new ArrayList<>();
				defaultValues.add(new Rank(200, "Administrator", "Admin", "#7a0d05", RankType.STAFF, false));
				defaultValues.add(new Rank(100, "Player", "Player", "#8c8484", RankType.USER, true));

				String s = Core.getInstance().getGson().toJson(defaultValues);
				BufferedWriter bw = new BufferedWriter(new FileWriter(ranksFile));
				bw.write(s);
				bw.close();

			} catch (IOException e) {
				Core.getInstance().log(getClass(), LogType.CRITICAL,
						"Could not create default ranks file: " + e.getMessage());
				return;
			}
		}

		// Load
		try {

			FileReader fr = new FileReader(this.ranksFile);
			List<Rank> loaded = Core.getInstance().getGson().fromJson(fr, new TypeToken<ArrayList<Rank>>() {
			}.getType());

			if (loaded != null) {
				this.ranks.addAll(loaded);
			}

		} catch (IOException e) {
			Core.getInstance().log(getClass(), LogType.CRITICAL, "Could not load ranks from file: " + e.getMessage());
			return;
		}

		Core.getInstance().log(this.getClass(), LogType.DEBUG, "Loaded " + this.ranks.size() + " Ranks");

	}

	public final Collection<Rank> getRanks() {
		return this.ranks;
	}

	public final Rank getRankByPriority(@Nonnull int id) {
		return this.ranks.stream().filter(rank -> rank.getPriority() == id).findAny().orElse(null);
	}

	public final List<Rank> getInRightOrder() {

		List<Integer> list = new ArrayList<>();

		for (Rank rank : this.ranks) {
			list.add(rank.getPriority());
		}

		Collections.sort(list, Collections.reverseOrder());

		List<Rank> ranks = new ArrayList<>();

		for (int i : list) {
			Rank rank = this.getRankByPriority(i);
			ranks.add(rank);
		}

		return ranks;
	}

	public final Rank getDefaultRank() {

		for (Rank rank : this.ranks) {
			if (rank.isDefault())
				return rank;
		}

		return null;
	}

	private final void createTables() {

		final Connection connection = this.getMySQL().getConnection();
		final String prefix = this.getMySQL().getTablePrefix();

		final Collection<String> statements = new ArrayList<>();
		statements.add("CREATE TABLE IF NOT EXISTS " + prefix + TableType.SAVE_TYPE.getTableName(false) + "("
				+ "id INT AUTO_INCREMENT NOT NULL, " + "name VARCHAR(50) NOT NULL, "
				+ "icon VARCHAR(100) NOT NULL DEFAULT 'PAPER', "
				+ "worldType ENUM('NORMAL', 'FLAT', 'NETHER', 'END', 'VOID') NOT NULL DEFAULT 'VOID', "
				+ "PRIMARY KEY (id));");

		statements.add("CREATE TABLE IF NOT EXISTS " + prefix + TableType.USERS.getTableName(false) + " ("
				+ "uuid CHAR(36) NOT NULL, " + "lastKnownName CHAR(16) NOT NULL, "
				+ "registered BIGINT(255) NOT NULL DEFAULT '" + System.currentTimeMillis() + "', "
				+ "lastLogin BIGINT(255) NOT NULL DEFAULT '-1', " + "lastLogout BIGINT(255) NOT NULL DEFAULT '-1', "
				+ "enabled BOOLEAN NOT NULL DEFAULT '1', " + "language VARCHAR(50) NOT NULL DEFAULT '"
				+ this.getDefaultLanguage().toString() + "', " + "immune BOOLEAN NOT NULL DEFAULT '0', "
				+ "freepass BOOLEAN NOT NULL DEFAULT '0', " + "PRIMARY KEY (uuid));");

		statements.add("CREATE TABLE IF NOT EXISTS " + prefix + TableType.STATS.getTableName(false) + " ("
				+ "uuid CHAR(36) NOT NULL, " + "saveType INT NOT NULL, " + "statistic VARCHAR(50) NOT NULL, "
				+ "amount INT NOT NULL DEFAULT '-1', " + "PRIMARY KEY (uuid, saveType, statistic), "
				+ "FOREIGN KEY (saveType) REFERENCES " + prefix + TableType.SAVE_TYPE.getTableName(false) + "(id));");

		statements.add("CREATE TABLE IF NOT EXISTS " + prefix + TableType.MAPS.getTableName(false) + " ("
				+ "id INT AUTO_INCREMENT NOT NULL, " + "name VARCHAR(50) NOT NULL, " + "saveType INT NOT NULL, "
				+ "icon VARCHAR(100) NOT NULL DEFAULT 'PAPER', " + "creator CHAR(36) NOT NULL, "
				+ "contributors VARCHAR(500), "
				+ "state ENUM('AWAITING_APPROVAL', 'APPROVED', 'DONE', 'FINISHED', 'DELETED', 'DAMAGED') NOT NULL DEFAULT 'AWAITING_APPROVAL', "
				+ "spawns MEDIUMTEXT, " + "teams BOOLEAN NOT NULL DEFAULT '0', " + "PRIMARY KEY(id, name, saveType), "
				+ "FOREIGN KEY (saveType) REFERENCES " + prefix + TableType.SAVE_TYPE.getTableName(false) + "(id));");

		statements.add("CREATE TABLE IF NOT EXISTS " + prefix + TableType.MOTD.getTableName(false) + " ("
				+ "line1 VARCHAR(250) NOT NULL, " + "line2 VARCHAR(250) NOT NULL, " + "staff CHAR(36), "
				+ "changed BIGINT(255) NOT NULL DEFAULT '-1'" + ");");

		statements.add("CREATE TABLE IF NOT EXISTS " + prefix + TableType.SETTINGS.getTableName(false) + " ("
				+ "setting VARCHAR(100) PRIMARY KEY NOT NULL, " + "value VARCHAR(100) NOT NULL, " + "staff CHAR(36), "
				+ "changed BIGINT(255) NOT NULL DEFAULT '-1'" + ");");

		statements.add("CREATE TABLE IF NOT EXISTS " + prefix + TableType.MAINTENANCE.getTableName(false) + " ("
				+ "uuid CHAR(36) PRIMARY KEY NOT NULL" + ");");

		statements.add("CREATE TABLE IF NOT EXISTS " + prefix + TableType.LICENSES_MASTER.getTableName(false) + " ("
				+ "id INT AUTO_INCREMENT NOT NULL, " + "saveType INT NOT NULL, " + "name VARCHAR(100) NOT NULL, "
				+ "created BIGINT(255) NOT NULL DEFAULT '" + System.currentTimeMillis() + "', "
				+ "expire BIGINT(255) NOT NULL DEFAULT '-1', " + "iconName VARCHAR(100) NOT NULL DEFAULT 'PAPER', "
				+ "PRIMARY KEY (id, saveType, name), " + "FOREIGN KEY (saveType) REFERENCES " + prefix
				+ TableType.SAVE_TYPE.getTableName(false) + "(id));");

		statements.add("CREATE TABLE IF NOT EXISTS " + prefix + TableType.LICENSES_USER.getTableName(false) + " ("
				+ "uuid CHAR(36) NOT NULL, " + "license INT NOT NULL, " + "issued BIGINT(255) NOT NULL DEFAULT '"
				+ System.currentTimeMillis() + "', " + "expire BIGINT(255) NOT NULL DEFAULT '-1', "
				+ "PRIMARY KEY (uuid, license), " + "FOREIGN KEY (license) REFERENCES " + prefix
				+ TableType.LICENSES_MASTER.getTableName(false) + "(id));");

		try {

			for (String s : statements) {

				PreparedStatement stmt = connection.prepareStatement(s);
				stmt.execute();

			}

		} catch (SQLException e) {
			this.log(this.getClass(), LogType.ERROR, "Could not create MySQL Data Tables: " + e.getMessage());
		}

	}

	public final void createDefaultSaveTypes() {

		if (!this.getMySQL().isConnected()) {
			this.log(this.getClass(), LogType.WARNING,
					"Could not create default SaveTypes: Connection towards MySQL Database is not established.");
			return;
		}

		if (!this.getSaveTypes().isEmpty()) {
			this.log(this.getClass(), LogType.WARNING,
					"Did not allow to create default SaveTypes because some do already exist.");
			return;
		}

		Collection<SaveType> defaultSaveTypes = new ArrayList<>(4);
		defaultSaveTypes.add(new SaveType(0, "SkyBlock", "GRASS_BLOCK", WorldType.VOID));
		defaultSaveTypes.add(new SaveType(0, "CityBuild", "IRON_PICKAXE", WorldType.NORMAL));
		defaultSaveTypes.add(new SaveType(0, "KnockOut", "STICK", WorldType.VOID));
		defaultSaveTypes.add(new SaveType(0, "BedWars", "RED_BED", WorldType.VOID));

		for (SaveType st : defaultSaveTypes) {

			try {

				PreparedStatement stmt = this.getMySQL().getConnection().prepareStatement("INSERT INTO "
						+ TableType.SAVE_TYPE.getTableName(true) + " (name, icon, worldType) VALUES (?, ?, ?);");
				stmt.setString(1, st.getName());
				stmt.setString(2, st.getIconName());
				stmt.setString(3, st.getWorldType().toString());

				stmt.execute();
				this.log(this.getClass(), LogType.INFO,
						"Created default SaveType '" + st.getName() + "' with icon '" + st.getIconName() + "'.");

			} catch (SQLException e) {
				this.log(this.getClass(), LogType.ERROR, "Could not create default SaveTypes/SQL: " + e.getMessage());
			}

		}

		this.loadSaveTypes();

	}

	private final void loadSaveTypes() {

		this.saveTypes.clear();

		try {

			PreparedStatement stmt = this.getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + TableType.SAVE_TYPE.getTableName(true));
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				int id = rs.getInt(1);
				String name = rs.getString(2);
				String iconName = rs.getString(3);
				WorldType worldType = WorldType.valueOf(rs.getString(4));

				SaveType st = new SaveType(id, name, iconName, worldType);
				this.saveTypes.add(st);

				this.log(this.getClass(), LogType.DEBUG, "Loaded SaveType '" + st.toString() + "'.");

			}

		} catch (SQLException e) {
			this.log(this.getClass(), LogType.ERROR, "Could not load SaveTypes from Database/SQL: " + e.getMessage());
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
	public final void log(@Nonnull Class<?> c, @Nonnull LogType level, @Nonnull String message) {
		this.log(this.getPluginName(), c, level, message);
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
	public final void log(@Nonnull String pluginName, @Nullable Class<?> c, @Nonnull LogType level,
			@Nonnull String message) {

		final String logMessage = "[" + pluginName + "/"
				+ (c == null ? "?" : this.logWithSimpleClassNames() ? c.getSimpleName() : c.getName()) + "/"
				+ level.toString() + "] " + message;

		if (this.getRunMode() == RunMode.LOCAL) {
			org.bukkit.Bukkit.getConsoleSender().sendMessage(level.getColor() + logMessage);

			try {

				if (Class.forName("at.peirleitner.core.SpigotMain") != null) {

					at.peirleitner.core.api.local.LogMessageCreateEvent event = new at.peirleitner.core.api.local.LogMessageCreateEvent(
							pluginName, c, level, message);
					SpigotMain.getInstance().getServer().getPluginManager().callEvent(event);

				}

			} catch (ClassNotFoundException | IllegalStateException e) {
				// Async Events will still print to console, even tho the User won't get a
				// message
			}

		} else {
			net.md_5.bungee.api.ProxyServer.getInstance().getConsole()
					.sendMessage(new net.md_5.bungee.api.chat.TextComponent(level.getColor() + logMessage));
		}

		// Create Webhook
		if (!message.equals(DISCORD_WEBHOOK_INVALID) && !message.equals(DISCORD_WEBHOOK_ERROR) && !(level == LogType.DEBUG)) {
			this.createWebhook("[" + c.getName() + "/" + level.toString() + "] " + message, DiscordWebHookType.LOG);
		}

	}

	/**
	 * @since 1.0.14
	 * @param message
	 */
	public final void createWebhook(@Nonnull String message, DiscordWebHookType type) {

		if(this.getRunMode() == RunMode.LOCAL) {
			
			try {
				
				new org.bukkit.scheduler.BukkitRunnable() {
					
					@Override
					public void run() {
						
						if (getSettingsManager() == null || !type.isEnabled())
							return;

						if (!type.getURL().startsWith(DISCORD_WEBHOOK_START_URL)) {
							log(getClass(), LogType.DEBUG, DISCORD_WEBHOOK_INVALID);
							return;
						}

						DiscordWebhook webhook = new DiscordWebhook(type.getURL());
						webhook.setContent(message);
						webhook.setUsername(getSettingsManager().getServerName());

						try {
							webhook.execute();
						} catch (IOException ex) {
//							log(getClass(), LogType.ERROR, DISCORD_WEBHOOK_ERROR.replace("{error}", ex.getMessage()));
						}
						
					}
				}.runTaskAsynchronously(SpigotMain.getInstance());
				
			}catch(org.bukkit.plugin.IllegalPluginAccessException ex) {
				// Plugin has disabled
			}
			
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

	// | Settings | \\

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
				.valueOf(this.getSettingsManager().getSetting(this.getPluginName(), "manager.settings.is-network"));
	}

	/**
	 * 
	 * @return Default language that will be assigned to a {@link User} upon
	 *         registration
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final Language getDefaultLanguage() {
		return Language.valueOf(
				this.getSettingsManager().getSetting(this.getPluginName(), "manager.settings.default-language"));
	}

	public final boolean logWithSimpleClassNames() {
		return this.getSettingsManager() == null ? true
				: Boolean.valueOf(this.getSettingsManager().getSetting(this.getPluginName(),
						"manager.settings.log-with-simple-class-names"));
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

	/**
	 * 
	 * @return Manager for Messages
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final LanguageManager getLanguageManager() {
		return this.languageManager;
	}

	/**
	 * Register default messages for both {@link RunMode}s
	 * 
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	private final void registerMessages() {

		// All
		for (LanguagePhrase phrase : LanguagePhrase.values()) {
			this.getLanguageManager().registerNewMessage(this.getPluginName(),
					"phrase." + phrase.toString().toLowerCase(), phrase.getDefaultValue());
		}
		
		this.getLanguageManager().registerNewMessage(this.getPluginName(), "notify.system.moderation.chatLog.review", "&9{0} &7reviewed the ChatLog &9{1}&7. Result: &9{2}&7.");
		
		this.getLanguageManager().registerNewMessage(this.getPluginName(), "system.moderation.chat-log-restriction-active", "&7You have been temporarily restricted from the Chat until our Staff has reviewed a recent ChatLog that has been issued against you. ID&8: &9{0}");
		this.getLanguageManager().registerNewMessage(this.getPluginName(), "system.moderation.chat-spam", "&7Your message is too similar to your previous one.");
		this.getLanguageManager().registerNewMessage(this.getPluginName(), "system.moderation.chat-caps", "&7Your message contains too many uppercase letters.");
		this.getLanguageManager().registerNewMessage(this.getPluginName(), "system.moderation.chat-cooldown", "&7You may only type a message every &9{0} &7seconds.");
		
		this.getLanguageManager().registerNewMessage(this.getPluginName(), "system.moderation.chatLog.review.error.already-has-review", "&7The ChatLog &9{0} &7has already been reviewed.");
		this.getLanguageManager().registerNewMessage(this.getPluginName(), "system.moderation.chatLog.review.success", "&7Successfully reviewed the ChatLog &9{0} &7with &9{1}&7.");
		
		if (this.getRunMode() == RunMode.NETWORK) {

		} else if (this.getRunMode() == RunMode.LOCAL) {

		}

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

	/**
	 * 
	 * @return System for Statistics
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final StatSystem getStatSystem() {
		return this.statSystem;
	}

	/**
	 * 
	 * @return GameMap System
	 * @since 1.0.3
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final GameMapSystem getGameMapSystem() {
		return this.gameMapSystem;
	}

	/**
	 * 
	 * @return MotdSystem
	 * @since 1.0.4
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final MotdSystem getMotdSystem() {
		return this.motdSystem;
	}

	/**
	 * 
	 * @return MaintenanceSystem
	 * @since 1.0.5
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final MaintenanceSystem getMaintenanceSystem() {
		return this.maintenanceSystem;
	}

	/**
	 * 
	 * @return {@link LicenseSystem}
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final LicenseSystem getLicenseSystem() {
		return this.licenseSystem;
	}

	/**
	 * 
	 * @return {@link EconomySystem}
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final EconomySystem getEconomySystem() {
		return this.economySystem;
	}

	/**
	 * 
	 * @return {@link ModerationSystem}
	 * @since 1.0.14
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final ModerationSystem getModerationSystem() {
		return this.moderationSystem;
	}

}
