package at.peirleitner.core.system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.CoreSystem;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.database.TableType;
import at.peirleitner.core.util.user.AvailableUserStatistic;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.User;
import at.peirleitner.core.util.user.UserStatistic;

/**
 * System that manages statistics of a {@link User}
 * 
 * @since 1.0.17
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class StatSystem implements CoreSystem {

	private final HashSet<AvailableUserStatistic> cachedStatistics;
	private final HashSet<UserStatistic> cachedUserStatistics;

	public StatSystem() {

		this.createTable();
		this.cachedStatistics = new HashSet<>();
		this.cachedUserStatistics = new HashSet<>();

		Core.getInstance().getSettingsManager().registerSetting(Core.getInstance().getPluginName(),
				"system.stats.enable-caching", "true");
		Core.getInstance().getSettingsManager().registerSetting(Core.getInstance().getPluginName(), "system.stats.show-stats-of-current-saveType-directly", "false");
		Core.getInstance().getSettingsManager().registerSetting(Core.getInstance().getPluginName(), "system.stats.show-back-item", "true");

		this.loadStatistics();
		this.loadUserStatistics();

	}

	private final void loadStatistics() {

		if (!this.isCachingEnabled()) {
			return;
		}

		this.getCachedStatistics().clear();

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + TableType.STATISTICS_AVAILABLE.getTableName(true));
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				AvailableUserStatistic statistic = this.getAvailableUserStatisticByResultSet(rs);
				this.getCachedStatistics().add(statistic);

			}

			Core.getInstance().log(getClass(), LogType.DEBUG,
					"Cached " + this.getCachedStatistics().size() + " available Statistics.");

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not load available Statistics from Database/SQL: " + e.getMessage());
		}

	}

	private final void loadUserStatistics() {

		if (!this.isCachingEnabled()) {
			return;
		}

		this.getCachedUserStatistics().clear();

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + TableType.STATISTICS_USER.getTableName(true));
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				UserStatistic statistic = this.getUserStatisticByResultSet(rs);
				this.getCachedUserStatistics().add(statistic);

			}

			Core.getInstance().log(getClass(), LogType.DEBUG,
					"Cached " + this.getCachedUserStatistics().size() + " User Statistics.");

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not load User Statistics from Database/SQL: " + e.getMessage());
		}

	}

	public final HashSet<AvailableUserStatistic> getCachedStatistics() {
		return cachedStatistics;
	}

	public final HashSet<UserStatistic> getCachedUserStatistics() {
		return cachedUserStatistics;
	}

	public final boolean isCachingEnabled() {
		return Core.getInstance().getSettingsManager().isSetting(Core.getInstance().getPluginName(),
				"system.stats.enable-caching");
	}

	/**
	 * 
	 * @param devName
	 * @param saveType
	 * @return If the given Statistic does already exist
	 * @since 1.0.17
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote This is case-insensitive
	 */
	public final boolean isStatistic(@Nonnull String devName, @Nonnull SaveType saveType) {

		if (this.isCachingEnabled()) {

			for (AvailableUserStatistic stat : this.getAvailableStatistics()) {
				if (stat.getDevName().equals(devName.toUpperCase()) && stat.getSaveType() == saveType) {
					return true;
				}
			}

			return false;

		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("SELECT * FROM "
					+ TableType.STATISTICS_AVAILABLE.getTableName(true) + " WHERE devName = ? AND saveType = ?");
			stmt.setString(1, devName.toUpperCase());
			stmt.setInt(2, saveType.getID());

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return true;
			} else {
				// Does not exist
				return false;
			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not check if the Statistic with Developer Name '"
					+ devName + "' exisits/SQL: " + e.getMessage());
			return false;
		}

	}

	/**
	 * 
	 * @return Available Statistics
	 * @since 1.0.17
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote This will return the cached values if {@link #isCachingEnabled()} is
	 *          set to <code>true</code>.
	 */
	public final AvailableUserStatistic[] getAvailableStatistics() {

		if (this.isCachingEnabled()) {
			AvailableUserStatistic[] stats = this.getCachedStatistics()
					.toArray(new AvailableUserStatistic[this.getCachedStatistics().size()]);
			return stats;
		}

		Collection<AvailableUserStatistic> available = new ArrayList<>();

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + TableType.STATISTICS_AVAILABLE.getTableName(true));
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				AvailableUserStatistic statistic = this.getAvailableUserStatisticByResultSet(rs);
				available.add(statistic);

			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get available statistics/SQL: " + e.getMessage());
			return null;
		}

		AvailableUserStatistic[] stats = available.toArray(new AvailableUserStatistic[available.size()]);
		return stats;
	}

	public final AvailableUserStatistic getAvailableUserStatisticByResultSet(@Nonnull ResultSet rs)
			throws SQLException {

		int id = rs.getInt(1);
		String devName = rs.getString(2);
		String displayName = rs.getString(3);
		String description = rs.getString(4);
		SaveType saveType = Core.getInstance().getSaveTypeByID(rs.getInt(5));
		long created = rs.getLong(6);
		boolean isEnabled = rs.getBoolean(7);
		String iconName = rs.getString(8);

		AvailableUserStatistic statistic = new AvailableUserStatistic();
		statistic.setID(id);
		statistic.setDevName(devName);
		statistic.setDisplayName(displayName);
		statistic.setDescription(description);
		statistic.setSaveType(saveType);
		statistic.setCreated(created);
		statistic.setEnabled(isEnabled);
		statistic.setIconName(iconName);

		return statistic;
	}

	public final AvailableUserStatistic getAvailableUserStatisticByDevName(@Nonnull String devName) {

		if (this.isCachingEnabled()) {

			for (AvailableUserStatistic aus : this.getCachedStatistics()) {
				if (aus.getDevName().equals(devName.toUpperCase())) {
					return aus;
				}
			}

			Core.getInstance().log(getClass(), LogType.ERROR,
					"Tried to get cached Statistic with DevName '" + devName + "', but no Result could be found.");
			return null;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
					"SELECT * FROM " + TableType.STATISTICS_AVAILABLE.getTableName(true) + " WHERE devName = ?");
			stmt.setString(1, devName.toUpperCase());

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				AvailableUserStatistic stat = this.getAvailableUserStatisticByResultSet(rs);
				return stat;

			} else {
				// No Result
				return null;
			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get Statistic by DevName '" + devName + "'/SQL: " + e.getMessage());
			return null;
		}

	}

	public final AvailableUserStatistic getAvailableUserStatisticByID(@Nonnull int id) {

		if (this.isCachingEnabled()) {

			for (AvailableUserStatistic aus : this.getCachedStatistics()) {
				if (aus.getID() == id) {
					return aus;
				}
			}

			Core.getInstance().log(getClass(), LogType.ERROR,
					"Tried to get cached Statistic with ID '" + id + "', but no Result could be found.");
			return null;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
					"SELECT * FROM " + TableType.STATISTICS_AVAILABLE.getTableName(true) + " WHERE id = ?");
			stmt.setInt(1, id);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				AvailableUserStatistic stat = this.getAvailableUserStatisticByResultSet(rs);
				return stat;

			} else {
				// No Result
				return null;
			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get Statistic by ID '" + id + "'/SQL: " + e.getMessage());
			return null;
		}

	}

	/**
	 * Register a new Statistic.
	 * 
	 * @param devName     - Development Name. This will also be the message key
	 *                    <i>(statistic.devName)</i>
	 * @param displayName - Name of this Statistics in {@link Language#ENGLISH}. A
	 *                    new entry inside the language manager will be created for
	 *                    translation.
	 * @param description - Description of this Statistics in
	 *                    {@link Language#ENGLISH}. A new entry inside the language
	 *                    manager will be created for translation.
	 * @param saveType
	 * @param iconName
	 * @return If the Statistic has been registered
	 * @since 1.0.17
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean registerStatistic(@Nonnull String pluginName, @Nonnull String devName,
			@Nonnull String displayName, @Nonnull String description, @Nonnull SaveType saveType,
			@Nonnull String iconName) {

		if (this.isStatistic(devName, saveType)) {
//			Core.getInstance().log(getClass(), LogType.DEBUG, "Could not register Statistic with Developer Name '"
//					+ devName + "': A Statistic with the given Name does already exist.");
			return false;
		}

		AvailableUserStatistic statistic = new AvailableUserStatistic();
		statistic.setDevName(devName.toUpperCase());
		statistic.setDisplayName(displayName);
		statistic.setDescription(description);
		statistic.setSaveType(saveType);
		statistic.setCreated(System.currentTimeMillis());
		statistic.setEnabled(false);
		statistic.setIconName(iconName);

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("INSERT INTO "
					+ TableType.STATISTICS_AVAILABLE.getTableName(true)
					+ " (devName, displayName, description, saveType, created, enabled, iconName) VALUES (?, ?, ?, ?, ?, ?, ?);",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, statistic.getDevName());
			stmt.setString(2, statistic.getDisplayName());
			stmt.setString(3, statistic.getDescription());
			stmt.setInt(4, statistic.getSaveType().getID());
			stmt.setLong(5, statistic.getCreated());
			stmt.setBoolean(6, statistic.isEnabled());
			stmt.setString(7, statistic.getIconName());

			stmt.executeUpdate();

			ResultSet rs = stmt.getGeneratedKeys();
			rs.next();

			int id = rs.getInt(1);
			statistic.setID(id);

			if (this.isCachingEnabled()) {
				this.getCachedStatistics().add(statistic);
			}

			Core.getInstance().getLanguageManager().registerNewMessage(pluginName,
					"statistic." + statistic.getDevName().toLowerCase() + ".displayName", statistic.getDisplayName());
			Core.getInstance().getLanguageManager().registerNewMessage(pluginName,
					"statistic." + statistic.getDevName().toLowerCase() + ".description", statistic.getDescription());

			Core.getInstance().log(getClass(), LogType.DEBUG,
					"Registered new Statistic '" + statistic.toString() + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not register new Statistic '" + statistic.toString() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	public final boolean addStatistic(@Nonnull UUID uuid, @Nonnull AvailableUserStatistic statistic,
			@Nonnull int amount) {

		if (!this.isStatistic(statistic.getDevName(), statistic.getSaveType())) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not add Statistic for User '" + uuid.toString()
					+ "': Invalid Statistic (Not registered) (" + statistic.toString() + "').");
			return false;
		}

		statistic = this.getAvailableUserStatisticByID(statistic.getID());

		if (!statistic.isEnabled()) {
			return false;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("INSERT INTO "
					+ TableType.STATISTICS_USER.getTableName(true)
					+ " (uuid, statistic, amount, firstAdded, lastAdded) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE amount = (amount + ?), lastAdded = ?");
			stmt.setString(1, uuid.toString());
			stmt.setInt(2, statistic.getID());
			stmt.setInt(3, amount);
			stmt.setLong(4, System.currentTimeMillis());
			stmt.setLong(5, System.currentTimeMillis());

			stmt.setInt(6, amount);
			stmt.setLong(7, System.currentTimeMillis());

			stmt.executeUpdate();

			Core.getInstance().log(getClass(), LogType.DEBUG,
					"Updated Statistic '" + statistic.getID() + "' for User '" + uuid.toString() + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not add Statistic '" + statistic.getID()
					+ "' for User '" + uuid.toString() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	public final boolean incrementStatistic(@Nonnull UUID uuid, @Nonnull AvailableUserStatistic statistic) {
		return this.addStatistic(uuid, statistic, 1);
	}

	public final Collection<UserStatistic> getStatistics(@Nonnull UUID uuid) {

		Collection<UserStatistic> statistics = new ArrayList<>();

		// Return from Cache if available
		if (this.isCachingEnabled()) {

			for (UserStatistic us : this.getCachedUserStatistics()) {
				if (us.getUUID().equals(uuid)) {
					statistics.add(us);
				}
			}

			return statistics;
		}

		// Return from Database
		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
					"SELECT * FROM " + TableType.STATISTICS_USER.getTableName(true) + " WHERE uuid = ?");
			stmt.setString(1, uuid.toString());

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				UserStatistic us = this.getUserStatisticByResultSet(rs);
				statistics.add(us);

			}

			return statistics;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get Statistics for User '" + uuid.toString() + "'/SQL: " + e.getMessage());
			return new ArrayList<>();
		}
	}

	public final Collection<UserStatistic> getStatistics(@Nonnull UUID uuid, @Nonnull SaveType saveType) {

		Collection<UserStatistic> statistics = new ArrayList<>();

		// Return from Cache if available
		if (this.isCachingEnabled()) {

			for (UserStatistic us : this.getCachedUserStatistics()) {
				if (us.getUUID().equals(uuid) && us.getStatistic().getSaveType().getID() == saveType.getID()) {
					statistics.add(us);
				}
			}

			return statistics;
		}

		// Return from Database
		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
					"SELECT * FROM " + TableType.STATISTICS_USER.getTableName(true) + " WHERE uuid = ?");
			stmt.setString(1, uuid.toString());

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				UserStatistic us = this.getUserStatisticByResultSet(rs);

				if (us.getStatistic().getSaveType().getID() != saveType.getID()) {
					continue;
				}

				statistics.add(us);

			}

			return statistics;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not get Statistics for User '" + uuid.toString()
					+ "' on SaveType '" + saveType.getID() + "'/SQL: " + e.getMessage());
			return new ArrayList<>();
		}
	}

	public final UserStatistic getStatistic(@Nonnull UUID uuid, @Nonnull AvailableUserStatistic statistic) {

		// Return from Cache if available
		if (this.isCachingEnabled()) {

			for (UserStatistic us : this.getCachedUserStatistics()) {

				if (us.getUUID().equals(uuid) && us.getStatistic().getID() == statistic.getID()) {
					return us;
				}

			}

		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("SELECT * FROM "
					+ TableType.STATISTICS_USER.getTableName(true) + " WHERE uuid = ? AND statistic = ?");
			stmt.setString(1, uuid.toString());
			stmt.setInt(2, statistic.getID());

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				UserStatistic us = this.getUserStatisticByResultSet(rs);
				return us;

			} else {
				// No Statistic
				return null;
			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not get User Statistic for User '"
					+ uuid.toString() + "' on Statistic '" + statistic.toString() + "'/SQL: " + e.getMessage());
			return null;
		}

	}

	public final UserStatistic getUserStatisticByResultSet(@Nonnull ResultSet rs) throws SQLException {

		UUID uuid = UUID.fromString(rs.getString(1));
		AvailableUserStatistic statistic = this.getAvailableUserStatisticByID(rs.getInt(2));
		int amount = rs.getInt(3);
		long firstAdded = rs.getLong(4);
		long lastAdded = rs.getLong(5);

		UserStatistic us = new UserStatistic(uuid);
		us.setStatistic(statistic);
		us.setAmount(amount);
		us.setFirstAdded(firstAdded);
		us.setLastAdded(lastAdded);

		return us;

	}

	@Override
	public void createTable() {

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("CREATE TABLE IF NOT EXISTS " + TableType.STATISTICS_AVAILABLE.getTableName(true)
							+ " (" + "id INT AUTO_INCREMENT NOT NULL, " + "devName VARCHAR(50) NOT NULL, "
							+ "displayName VARCHAR(30) NOT NULL, " + "description VARCHAR(300) NOT NULL, "
							+ "saveType INT NOT NULL, " + "created BIGINT(255) NOT NULL, "
							+ "enabled BOOLEAN NOT NULL DEFAULT '0', "
							+ "iconName VARCHAR(100) NOT NULL DEFAULT 'PAPER', "
							+ "PRIMARY KEY (id, devName, displayName, saveType), "
							+ "FOREIGN KEY (saveType) REFERENCES " + TableType.SAVE_TYPE.getTableName(true) + "(id));");

			stmt.execute();

			stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("CREATE TABLE IF NOT EXISTS " + TableType.STATISTICS_USER.getTableName(true)
							+ " (" + "uuid CHAR(36) NOT NULL, " + "statistic INT NOT NULL, " + "amount INT NOT NULL, "
							+ "firstAdded BIGINT(255) NOT NULL, " + "lastAdded BIGINT(255) NOT NULL, "
							+ "PRIMARY KEY (uuid, statistic), " + "FOREIGN KEY (statistic) REFERENCES "
							+ TableType.STATISTICS_AVAILABLE.getTableName(true) + "(id));");

			stmt.execute();

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not create Table for StatSystem/SQL: " + e.getMessage());
		}

	}

	@Override
	public TableType getTableType() {
		return null;
	}

}
