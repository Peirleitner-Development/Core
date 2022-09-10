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
import at.peirleitner.core.system.StatSystem;
import at.peirleitner.core.system.UserSystem;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.RunMode;
import at.peirleitner.core.util.database.CredentialsFile;
import at.peirleitner.core.util.database.MySQL;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.local.Rank;
import at.peirleitner.core.util.local.RankType;
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
	private final Gson gson;
	private Collection<SaveType> saveTypes;
	private final Collection<Rank> ranks;
	private File ranksFile;

	private final String table_saveType = "saveType";
	private final String table_users = "users";
	private final String table_stats = "stats";
	private final String table_shop = "shop";
	private final String table_maps = "maps";

	// Manager
	private SettingsManager settingsManager;
	private LanguageManager languageManager;

	// System
	private UserSystem userSystem;
	private StatSystem statSystem;

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

		// System
		this.userSystem = new UserSystem();
		this.statSystem = new StatSystem();

		this.log(this.getClass(), LogType.INFO, "Successfully enabled the Core instance with RunMode " + runMode
				+ ". Network-Mode is set to " + this.isNetwork() + ".");

		// Checks
		if (this.getSettingsManager().getSaveType() == null) {
			this.log(this.getClass(), LogType.WARNING,
					"SaveType has not been set inside '"
							+ this.getSettingsManager().getFile(this.getPluginName()).getPath()
							+ "', database interaction will not work on some systems until this has been set.");
		} else {
			this.log(this.getClass(), LogType.INFO,
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

	private final String getTablePrefix() {
		return this.getMySQL().isConnected() ? this.getMySQL().getTablePrefix() : "NOT_CONNECTED_";
	}

	/**
	 * 
	 * @return {@link #table_saveType}
	 * @since 1.0.2
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getTableSaveType() {
		return this.getTablePrefix() + table_saveType;
	}

	/**
	 * 
	 * @return {@link #table_users}
	 * @since 1.0.2
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getTableUsers() {
		return this.getTablePrefix() + table_users;
	}

	/**
	 * 
	 * @return {@link #table_stats}
	 * @since 1.0.2
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getTableStats() {
		return this.getTablePrefix() + table_stats;
	}

	/**
	 * 
	 * @return {@link #table_shop}
	 * @since 1.0.2
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getTableShop() {
		return this.getTablePrefix() + table_shop;
	}

	/**
	 * 
	 * @return {@link #table_maps}
	 * @since 1.0.2
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getTableMaps() {
		return this.getTablePrefix() + table_maps;
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
				// TODO Auto-generated catch block
				e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Core.getInstance().log(this.getClass(), LogType.INFO, "Loaded " + this.ranks.size() + " Ranks");

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
		statements.add("CREATE TABLE IF NOT EXISTS " + prefix + this.table_saveType + "("
				+ "id INT AUTO_INCREMENT NOT NULL, " + "name VARCHAR(50) NOT NULL, "
				+ "icon VARCHAR(100) NOT NULL DEFAULT 'PAPER', " + "PRIMARY KEY (id));");
		statements.add("CREATE TABLE IF NOT EXISTS " + prefix + this.table_users + " (" + "uuid CHAR(36) NOT NULL, "
				+ "lastKnownName CHAR(16) NOT NULL, " + "registered BIGINT(255) NOT NULL DEFAULT '"
				+ System.currentTimeMillis() + "', " + "lastLogin BIGINT(255) NOT NULL DEFAULT '-1', "
				+ "lastLogout BIGINT(255) NOT NULL DEFAULT '-1', " + "enabled BOOLEAN NOT NULL DEFAULT '1', "
				+ "language VARCHAR(50) NOT NULL DEFAULT '" + this.getDefaultLanguage().toString() + "', "
				+ "immune BOOLEAN NOT NULL DEFAULT '0', " + "freepass BOOLEAN NOT NULL DEFAULT '0', "
				+ "PRIMARY KEY (uuid));");
		statements.add("CREATE TABLE IF NOT EXISTS " + prefix + this.table_stats + " (" + "uuid CHAR(36) NOT NULL, "
				+ "saveType INT NOT NULL, " + "statistic VARCHAR(50) NOT NULL, " + "amount INT NOT NULL DEFAULT '-1', "
				+ "PRIMARY KEY (uuid, saveType, statistic), " + "FOREIGN KEY (saveType) REFERENCES " + prefix
				+ this.table_saveType + "(id));");
		statements.add("CREATE TABLE IF NOT EXISTS " + prefix + this.table_maps + " (" + "name VARCHAR(50) NOT NULL, "
				+ "saveType INT NOT NULL, " + "icon VARCHAR(100) NOT NULL DEFAULT 'PAPER', "
				+ "creator CHAR(36) NOT NULL, " + "contributors VARCHAR(500), "
				+ "state ENUM('AWAITING_APPROVAL', 'APPROVED', 'DONE', 'FINISHED', 'DELETED', 'DAMAGED') NOT NULL DEFAULT 'AWAITING_APPROVAL', "
				+ "spawns MEDIUMTEXT NOT NULL, " + "teams BOOLEAN NOT NULL DEFAULT '0', "
				+ "PRIMARY KEY(name, saveType), " + "FOREIGN KEY (saveType) REFERENCES " + prefix + this.table_saveType
				+ "(id));");

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
		defaultSaveTypes.add(new SaveType(0, "SkyBlock", "GRASS_BLOCK"));
		defaultSaveTypes.add(new SaveType(0, "CityBuild", "IRON_PICKAXE"));
		defaultSaveTypes.add(new SaveType(0, "KnockOut", "STICK"));
		defaultSaveTypes.add(new SaveType(0, "BedWars", "RED_BED"));

		for (SaveType st : defaultSaveTypes) {

			try {

				PreparedStatement stmt = this.getMySQL().getConnection().prepareStatement("INSERT INTO "
						+ this.getMySQL().getTablePrefix() + this.table_saveType + " (name, icon) VALUES (?, ?);");
				stmt.setString(1, st.getName());
				stmt.setString(2, st.getIconName());

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
					.prepareStatement("SELECT * FROM " + this.getMySQL().getTablePrefix() + this.table_saveType);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				int id = rs.getInt(1);
				String name = rs.getString(2);
				String iconName = rs.getString(3);

				SaveType st = new SaveType(id, name, iconName);
				this.saveTypes.add(st);

				this.log(this.getClass(), LogType.DEBUG, "Loaded SaveType '" + st.getIconName() + "' with name '"
						+ st.getName() + "' and icon '" + st.getIconName() + "'.");

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

	@Deprecated
	private final void registerMessages() {

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

}
