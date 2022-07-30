package at.peirleitner.core.system;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.Setting;
import at.peirleitner.core.util.user.Language;
import at.peirleitner.core.util.user.User;

/**
 * This class handles all (database) interactions of a {@link User}
 * 
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 *
 */
public final class UserSystem {

	private Collection<User> cachedUsers;

	public UserSystem() {

		// Initialize
		this.cachedUsers = new ArrayList<>();

		// Set default settings
		Core.getInstance().getSettingsManager().create(
				new Setting<Boolean>(Core.getInstance().getPluginName(), "system.user-system.enable-caching", true));

	}

	/**
	 * 
	 * @return Collection of cached Users
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote This will <b>always</b> return an empty collection if
	 *          {@link #isCachingEnabled()} is set to <code>false</code>.
	 */
	public final Collection<User> getCachedUsers() {
		return this.cachedUsers;
	}

	/**
	 * 
	 * @return If caching is enabled
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	private final boolean isCachingEnabled() {
		return (boolean) Core.getInstance().getSettingsManager()
				.getByName(Core.getInstance().getPluginName(), "system.user-system.enable-caching").getValue();
	}

	/**
	 * Update the lastKnownName of a {@link User}
	 * 
	 * @param user - User
	 * @param name - New name
	 * @return If the lastKnownName has been updated
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @see User#getLastKnownName()
	 */
	public final boolean setLastKnownName(@Nonnull User user, @Nonnull String name) {

		// Return if the name is the same
		if (user.getLastKnownName().equals(name)) {
			Core.getInstance().log(LogType.DEBUG, "Did not update lastKnownName of user '" + user.getUUID().toString()
					+ "' to '" + name + "': Name is the same.");
			return false;
		}

		// Database Query
		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("UPDATE "
					+ Core.getInstance().getMySQL().getTablePrefix() + "players SET lastKnownName = ? WHERE uuid = ?");
			stmt.setString(1, name);
			stmt.setString(2, user.getUUID().toString());

			stmt.execute();
			Core.getInstance().log(LogType.DEBUG,
					"Updated lastKnownName of '" + user.getUUID().toString() + "' to '" + name + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(LogType.WARNING, "Could not update lastKnownName for user '"
					+ user.getLastKnownName() + "' to '" + name + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	/**
	 * Update the lastLogin of a {@link User}
	 * 
	 * @param user      - User
	 * @param lastLogin - New TimeStamp
	 * @return If the lastLogin has been updated
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @see User#getLastLogin()
	 */
	public final boolean setLastLogin(@Nonnull User user, @Nonnull long lastLogin) {

		// Return if the name is the same
		if (user.getLastLogin() == lastLogin) {
			Core.getInstance().log(LogType.DEBUG, "Did not update lastLogin of user '" + user.getUUID().toString()
					+ "' to '" + lastLogin + "': TimeStamp is the same.");
			return false;
		}

		// Database Query
		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("UPDATE "
					+ Core.getInstance().getMySQL().getTablePrefix() + "players SET lastLogin = ? WHERE uuid = ?");
			stmt.setLong(1, lastLogin);
			stmt.setString(2, user.getUUID().toString());

			stmt.execute();
			Core.getInstance().log(LogType.DEBUG,
					"Updated lastLogin of '" + user.getUUID().toString() + "' to '" + lastLogin + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(LogType.WARNING, "Could not update lastLogin for user '" + user.getLastKnownName()
					+ "' to '" + lastLogin + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	/**
	 * Update the lastLogout of a {@link User}
	 * 
	 * @param user       - User
	 * @param lastLogout - New TimeStamp
	 * @return If the lastLogout has been updated
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @see User#getlastLogout()
	 */
	public final boolean setLastLogout(@Nonnull User user, @Nonnull long lastLogout) {

		// Return if the name is the same
		if (user.getLastLogout() == lastLogout) {
			Core.getInstance().log(LogType.DEBUG, "Did not update lastLogout of user '" + user.getUUID().toString()
					+ "' to '" + lastLogout + "': TimeStamp is the same.");
			return false;
		}

		// Database Query
		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("UPDATE "
					+ Core.getInstance().getMySQL().getTablePrefix() + "players SET lastLogout = ? WHERE uuid = ?");
			stmt.setLong(1, lastLogout);
			stmt.setString(2, user.getUUID().toString());

			stmt.execute();
			Core.getInstance().log(LogType.DEBUG,
					"Updated lastLogout of '" + user.getUUID().toString() + "' to '" + lastLogout + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(LogType.WARNING, "Could not update lastLogout for user '" + user.getLastKnownName()
					+ "' to '" + lastLogout + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	/**
	 * Update the enabled state of a {@link User}
	 * 
	 * @param user    - User
	 * @param enabled - New State
	 * @return If the state has been updated
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean setEnabled(@Nonnull User user, @Nonnull boolean enabled) {

		// Return if enabled state is the same
		if (user.isEnabled() && enabled || !user.isEnabled() && !enabled) {
			Core.getInstance().log(LogType.DEBUG, "Did not update enabled state of user '" + user.getUUID().toString()
					+ ": State is already set to '" + enabled + "'.");
			return false;
		}

		// Database Query
		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("UPDATE "
					+ Core.getInstance().getMySQL().getTablePrefix() + "players SET enabled = ? WHERE uuid = ?");
			stmt.setBoolean(1, enabled);
			stmt.setString(2, user.getUUID().toString());

			stmt.execute();
			Core.getInstance().log(LogType.DEBUG,
					"Updated enabled state of '" + user.getUUID().toString() + "' to '" + enabled + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(LogType.WARNING, "Could not update enabled state for user '"
					+ user.getLastKnownName() + "' to '" + enabled + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	/**
	 * 
	 * @return Default language that will be assigned to a {@link User} upon
	 *         registration
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final Language getDefaultLanguage() {
		return (Language) Core.getInstance().getSettingsManager()
				.getByName(Core.getInstance().getPluginName(), "system.user-system.default-language").getValue();
	}

}
