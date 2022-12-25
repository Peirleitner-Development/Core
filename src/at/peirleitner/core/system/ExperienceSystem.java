package at.peirleitner.core.system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.CoreSystem;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.RunMode;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.database.TableType;
import at.peirleitner.core.util.user.User;
import at.peirleitner.core.util.user.UserExperience;

/**
 * This System is used to grant {@link SaveType}-wide Experience to a
 * {@link User} for measurement against other Users.
 * 
 * @since 1.0.18
 * @author Markus Peirleitner (Rengobli)
 *
 */
public final class ExperienceSystem implements CoreSystem {

	private HashSet<UserExperience> cachedExperience;

	public ExperienceSystem() {
		this.createTable();

		this.cachedExperience = new HashSet<>();

		Core.getInstance().getSettingsManager().registerSetting(Core.getInstance().getPluginName(),
				"system.experience.enable-caching", "true");
	}

	public final boolean isCachingEnabled() {
		return Core.getInstance().getSettingsManager().isSetting(Core.getInstance().getPluginName(),
				"system.experience.enable-caching");
	}

	/**
	 * 
	 * @return Currently cached experience
	 * @since 1.0.18
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote Will be empty if {@link #isCachingEnabled()} is set to
	 *          <code>false</code>.
	 */
	public final UserExperience[] getCachedExperience() {
		UserExperience[] arr = new UserExperience[this.cachedExperience.size()];
		return this.cachedExperience.toArray(arr);
	}

	public final UserExperience[] getExperience(@Nonnull UUID uuid) {

		HashSet<UserExperience> experience = new HashSet<>();

		// Return from Cache if available
		if (this.isCachingEnabled()) {

			for (UserExperience ue : this.getCachedExperience()) {
				if (ue.getUUID().equals(uuid)) {
					experience.add(ue);
				}
			}

			if (!experience.isEmpty()) {
				UserExperience[] arr = new UserExperience[experience.size()];
				return experience.toArray(arr);
			}

		}

		// Return from Database
		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + this.getTableType().getTableName(true) + " WHERE uuid = ?");
			stmt.setString(1, uuid.toString());

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				UserExperience ue = this.getByResultSet(rs);
				experience.add(ue);

			}

			if (this.isCachingEnabled()) {
				this.cachedExperience.addAll(experience);
			}

			UserExperience[] arr = new UserExperience[experience.size()];
			return experience.toArray(arr);

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get Experience for User '" + uuid.toString() + "'/SQL: " + e.getMessage());
			return null;
		}

	}

	public final UserExperience getExperience(@Nonnull UUID uuid, @Nonnull SaveType saveType) {

		// Return from Cache if available
		if (this.isCachingEnabled()) {

			for (UserExperience ue : this.getCachedExperience()) {
				if (ue.getUUID().equals(uuid) && ue.getSaveType().getID() == saveType.getID()) {
					return ue;
				}
			}

		}

		// Return from Database
		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
					"SELECT * FROM " + this.getTableType().getTableName(true) + " WHERE uuid = ? AND saveType = ?");
			stmt.setString(1, uuid.toString());
			stmt.setInt(2, saveType.getID());

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				UserExperience ue = this.getByResultSet(rs);

				if (this.isCachingEnabled()) {
					this.cachedExperience.add(ue);
				}

				return ue;

			} else {
				// Create new default UserExperience Object
				return new UserExperience(uuid, saveType, 1, 0, 0);
			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not get Experience for User '" + uuid.toString()
					+ "' on SaveType '" + saveType.getID() + "'/SQL: " + e.getMessage());
			return null;
		}

	}

	public final boolean isCached(@Nonnull UUID uuid) {
		return this.getFromCache(uuid) == null ? false : true;
	}

	// A single Core instance can only run on one SaveType, that's why this works.
	public final UserExperience getFromCache(@Nonnull UUID uuid) {
		return this.cachedExperience.stream().filter(ue -> ue.getUUID().equals(uuid)).findAny().orElse(null);
	}

	/**
	 * Add Experience to a {@link User}. This will automatically increase normal and
	 * prestige levels if the given criteria is met.<br>
	 * If caching has been enabled, the data will be updated towards the database on
	 * server disconnect or crash.
	 * 
	 * @param uuid     - UUID to add the Experience for
	 * @param saveType - SaveType to add the Experience to
	 * @param amount   - Amount of Experience added
	 * @param message  - If a message should be displayed
	 * @return If the Experience has been added successfully
	 * @since 1.0.18
	 * @author Markus Peirleitner (Rengobli)
	 * @see #getRequiredExperienceForLevelUp(int)
	 * @see #getRequiredLevelForPrestige()
	 * @see #isCachingEnabled()
	 */
	public final boolean addExperience(@Nonnull UUID uuid, @Nonnull SaveType saveType, @Nonnull int amount,
			@Nonnull boolean message) {

		User user = Core.getInstance().getUserSystem().getUser(uuid);

		UserExperience ue = this.getExperience(uuid, saveType);
		int newExperience = ue.getExperience() + amount;

		int currentLevel = ue.getLevel();
		int newLevel = currentLevel;

		int currentPrestige = ue.getPrestige();
		int newPrestige = currentPrestige;

		while (newExperience >= this.getRequiredExperienceForLevelUp(newLevel)) {
			newExperience -= this.getRequiredExperienceForLevelUp(newLevel);
			newLevel++;
		}

		while (newLevel >= this.getRequiredLevelForPrestige()) {
			newLevel -= this.getRequiredLevelForPrestige();
			newPrestige++;
		}

		boolean isLevelUp = (currentLevel == newLevel ? false : true);
		boolean isPrestigeLevelUp = (ue.getPrestige() == newPrestige ? false : true);

		if (this.isCachingEnabled()) {

			ue.setExperience(newExperience);
			ue.setLevel(newLevel);
			ue.setPrestige(newPrestige);

			this.cachedExperience.remove(ue);
			this.cachedExperience.add(ue);

			if (message) {

				if (!isLevelUp && !isPrestigeLevelUp) {
					user.sendMessage(Core.getInstance().getPluginName(), "system.experience.experience-add",
							Arrays.asList("" + amount), true);
				} else if (isLevelUp && !isPrestigeLevelUp) {
					user.sendMessage(Core.getInstance().getPluginName(), "system.experience.level-up",
							Arrays.asList("" + currentLevel, "" + newLevel), false);
				} else if (isLevelUp && isPrestigeLevelUp) {
					user.sendMessage(Core.getInstance().getPluginName(), "system.experience.prestige-level-up",
							Arrays.asList("" + currentPrestige, "" + newPrestige), false);
				} else {
					Core.getInstance().log(getClass(), LogType.WARNING,
							"Could not get message scenario for experience addition of User '" + uuid.toString()
									+ "', not sending any message.");
				}

			}

			if (Core.getInstance().getRunMode() == RunMode.LOCAL) {

				org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(uuid);
				player.playSound(player.getLocation(), isLevelUp ? org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE
						: org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);

			}

//			Core.getInstance().log(getClass(), LogType.DEBUG, "Old: " + ue.toString() + ", New EXP: " + newExperience
//					+ ", Level: " + newLevel + ", Prestige: " + newPrestige + " (Cached)");
			return true;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("INSERT INTO "
					+ this.getTableType().getTableName(true)
					+ " (uuid, saveType, level, experience, prestige) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE level = ?, experience = ?, prestige = ?");
			stmt.setString(1, uuid.toString());
			stmt.setInt(2, saveType.getID());
			stmt.setInt(3, newLevel);
			stmt.setInt(4, newExperience);
			stmt.setInt(5, newPrestige);

			stmt.setInt(6, newLevel);
			stmt.setInt(7, newExperience);
			stmt.setInt(8, newPrestige);

			int update = stmt.executeUpdate();

			if (update > 0) {

				if (message) {

					if (!isLevelUp && !isPrestigeLevelUp) {
						user.sendMessage(Core.getInstance().getPluginName(), "system.experience.experience-add",
								Arrays.asList("" + amount), true);
					} else if (isLevelUp && !isPrestigeLevelUp) {
						user.sendMessage(Core.getInstance().getPluginName(), "system.experience.level-up",
								Arrays.asList("" + currentLevel, "" + newLevel), false);
					} else if (isLevelUp && isPrestigeLevelUp) {
						user.sendMessage(Core.getInstance().getPluginName(), "system.experience.prestige-level-up",
								Arrays.asList("" + currentPrestige, "" + newPrestige), false);
					} else {
						Core.getInstance().log(getClass(), LogType.WARNING,
								"Could not get message scenario for experience addition of User '" + uuid.toString()
										+ "', not sending any message.");
					}

				}

				if (Core.getInstance().getRunMode() == RunMode.LOCAL) {

					org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(uuid);
					player.playSound(player.getLocation(), isLevelUp ? org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE
							: org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);

				}

//				Core.getInstance().log(getClass(), LogType.DEBUG, "Old: " + ue.toString() + ", New EXP: "
//						+ newExperience + ", Level: " + newLevel + ", Prestige: " + newPrestige);
				return true;
			} else {
				Core.getInstance().log(getClass(), LogType.DEBUG,
						"Adding experience returned no updated rows for User '" + uuid.toString() + "' on SaveType '"
								+ saveType.getID() + "'.");
				return false;
			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not add Experience for User '" + uuid.toString()
					+ "' on SaveType '" + saveType.getID() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	/**
	 * Update cached Experience towards the Database.
	 * 
	 * @param uuid - UUID to update
	 * @return If the Update was successful
	 * @since 1.0.18
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote This should only be used by system internal resources, such as
	 *          quitting or server crashing.
	 */
	public final boolean updateCacheToDatabase(@Nonnull UUID uuid) {

		if (!this.isCached(uuid)) {
//			Core.getInstance().log(getClass(), LogType.DEBUG,
//					"Not updating experience cache of User '" + uuid.toString() + "': Nothing cached.");
			return false;
		}

		UserExperience ue = this.getFromCache(uuid);

		if (ue.isDefault()) {
//			Core.getInstance().log(getClass(), LogType.DEBUG,
//					"Not updating experience cache of User '" + uuid.toString() + "': Nothing cached (Default Object).");
			return false;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("INSERT INTO "
					+ this.getTableType().getTableName(true)
					+ " (uuid, saveType, level, experience, prestige) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE level = ?, experience = ?, prestige = ?");
			stmt.setString(1, uuid.toString());
			stmt.setInt(2, ue.getSaveType().getID());
			stmt.setInt(3, ue.getLevel());
			stmt.setInt(4, ue.getExperience());
			stmt.setInt(5, ue.getPrestige());

			stmt.setInt(6, ue.getLevel());
			stmt.setInt(7, ue.getExperience());
			stmt.setInt(8, ue.getPrestige());

			int update = stmt.executeUpdate();

			if (update > 0) {
//				Core.getInstance().log(getClass(), LogType.DEBUG, "Updated Experience of User '" + uuid.toString() + "' from Cache towards the Database.");
				this.cachedExperience.remove(ue);
				return true;
			} else {
				Core.getInstance().log(getClass(), LogType.WARNING, "Could not update Experience of User '"
						+ uuid.toString() + "' from Cache towards the Database: No rows updated.");
				return false;
			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not update Experience of User '" + uuid.toString()
					+ "' from Cache towards the Database/SQL: " + e.getMessage());
			return false;
		}

	}

	public final int getRequiredExperienceForLevelUp(@Nonnull int currentLevel) {
		return currentLevel * 100;
	}

	public final int getRequiredLevelForPrestige() {
		return 100;
	}

	public final UserExperience getByResultSet(@Nonnull ResultSet rs) throws SQLException {

		UUID uuid = UUID.fromString(rs.getString(1));
		SaveType saveType = Core.getInstance().getSaveTypeByID(rs.getInt(2));
		int level = rs.getInt(3);
		int experience = rs.getInt(4);
		int prestige = rs.getInt(5);

		return new UserExperience(uuid, saveType, level, experience, prestige);
	}

	@Override
	public void createTable() {

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getTableType().getTableName(true) + " ("
							+ "uuid CHAR(36) NOT NULL, " + "saveType INT NOT NULL, " + "level SMALLINT NOT NULL, "
							+ "experience MEDIUMINT NOT NULL, " + "prestige TINYINT NOT NULL, "
							+ "PRIMARY KEY(uuid, saveType), " + "FOREIGN KEY (saveType) REFERENCES "
							+ TableType.SAVE_TYPE.getTableName(true) + "(id));");

			stmt.execute();

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not create Table/SQL: " + e.getMessage());
		}

	}

	@Override
	public TableType getTableType() {
		return TableType.EXPERIENCE;
	}

}
