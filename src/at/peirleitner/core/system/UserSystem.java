package at.peirleitner.core.system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.TableType;
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

	private final String table = TableType.USERS.getTableName(true);
	private Collection<User> cachedUsers;

	public UserSystem() {

		// Initialize
		this.cachedUsers = new ArrayList<>();

		// Set default settings
		Core.getInstance().getSettingsManager().setSetting(Core.getInstance().getPluginName(), "system.user.enable-caching", "true");
		
		// Load Data into Cache
		if(this.isCachingEnabled()) {
//			this.loadUsersFromDatabase(); - Don't load, just add/remove them on Join/Quit
		}

	}

	private final User getUserFromCache(@Nonnull UUID uuid) {
		return this.getCachedUsers().stream().filter(user -> user.getUUID().equals(uuid)).findAny().orElse(null);
	}

	private final User getUserFromDatabase(@Nonnull UUID uuid) {

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + table + " WHERE uuid = ?");
			stmt.setString(1, uuid.toString());

			ResultSet rs = stmt.executeQuery();
			
			if(!rs.next()) {
				Core.getInstance().log(this.getClass(), LogType.DEBUG, "Could not get User Object for UUID '" + uuid.toString() + "': ResultSet does not have any entries.");
				return null;
			} else {
				return this.getUserFromResultSet(rs);
			}

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not get User Object for UUID '" + uuid.toString() + "'/SQL: " + e.getMessage());
			return null;
		}

	}

	/**
	 * Load all Users from the Database inside the {@link #getCachedUsers()} collection.
	 * @return If all Users have been moved inside the Cache
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote This should not be used. Players are cached and removed on Join/Quit, depending on the {@link Core#isNetwork()} mode.
	 * @apiNote <b>This will override the existing {@link #getCachedUsers()} collection</b>.
	 */
	@SuppressWarnings("unused")
	private final boolean loadUsersFromDatabase() {
		
		this.getCachedUsers().clear();
		
		try {
			
			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("SELECT * FROM " + table);
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()) {
				this.getCachedUsers().add(this.getUserFromResultSet(rs));
			}
			
			Core.getInstance().log(this.getClass(), LogType.INFO, "Cached " + this.getCachedUsers().size() + " Users from Database.");
			return true;
			
		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.WARNING, "Could not load Users into Cache/SQL: " + e.getMessage());
			return false;
		}
		
	}
	
	private final User getUserFromResultSet(@Nonnull ResultSet rs) throws SQLException {

		UUID uuid = UUID.fromString(rs.getString(1));
		String lastKnownName = rs.getString(2);
		long registered = rs.getLong(3);
		long lastLogin = rs.getLong(4);
		long lastLogout = rs.getLong(5);
		boolean enabled = rs.getBoolean(6);
		Language language = Language.valueOf(rs.getString(7));
		boolean immune = rs.getBoolean(8);
		boolean freepass = rs.getBoolean(9);

		User user = new User(uuid, lastKnownName, registered, lastLogin, lastLogout, enabled, language, immune, freepass);

		return user;
	}

	public final User getUser(@Nonnull UUID uuid) {
		return this.getUserFromCache(uuid) == null ? this.getUserFromDatabase(uuid) : this.getUserFromCache(uuid);
	}
	
	public final User getByLastKnownName(@Nonnull String lastKnownName) {
		
		if(this.isCachingEnabled() && !this.getCachedUsers().isEmpty()) {
			
			for(User user : this.getCachedUsers()) {
				if(user.getLastKnownName().equalsIgnoreCase(lastKnownName)) return user;
			}
			
		}
		
		try {
			
			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("SELECT * FROM " + table + " WHERE lastKnownName = ?");
			stmt.setString(1, lastKnownName);
			
			ResultSet rs = stmt.executeQuery();
			
			if(rs.next()) {
				return getUserFromResultSet(rs);
			} else {
				Core.getInstance().log(this.getClass(), LogType.DEBUG, "Did not return User by last Known Name since none can be found");
				return null;
			}
			
		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not get User '" + lastKnownName + "' by last known name/SQL: " + e.getMessage());
			return null;
		}
		
	}

	public final boolean isRegistered(@Nonnull UUID uuid) {
		return this.getUser(uuid) == null ? false : true;
	}

	public final boolean register(@Nonnull UUID uuid, @Nonnull String name) {

		// Return if the User has already been registered
		if(this.isRegistered(uuid)) {
//			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Did not attempt to register new User for UUID '" + uuid.toString() + "': Player already registered.");
			return false;
		}
		
		// Database Query
		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("INSERT INTO " + table + " (uuid, lastKnownName) VALUES (?, ?);");
			stmt.setString(1, uuid.toString());
			stmt.setString(2, name);

			stmt.execute();

			if (this.isCachingEnabled()) {
				User user = new User(uuid, name, System.currentTimeMillis(), -1, -1, true,
						Core.getInstance().getDefaultLanguage(), false, false);
				this.getCachedUsers().add(user);
			}

			Core.getInstance().log(this.getClass(), LogType.INFO,
					"Registered new User Object for UUID '" + uuid.toString() + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not register new User Object for UUID '" + uuid.toString() + "'/SQL: " + e.getMessage());
			return false;
		}

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
	public final boolean isCachingEnabled() {
		return Boolean.valueOf(Core.getInstance().getSettingsManager().getSetting(Core.getInstance().getPluginName(), "system.user.enable-caching"));
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
			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Did not update lastKnownName of user '"
					+ user.getUUID().toString() + "' to '" + name + "': Name is the same.");
			return false;
		}

		// Database Query
		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + table + " SET lastKnownName = ? WHERE uuid = ?");
			stmt.setString(1, name);
			stmt.setString(2, user.getUUID().toString());

			stmt.execute();
			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Updated lastKnownName of '" + user.getUUID().toString() + "' to '" + name + "'.");

			if (this.isCachingEnabled()) {
				user.setLastKnownName(name);
			}

			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.WARNING, "Could not update lastKnownName for user '"
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
			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Did not update lastLogin of user '"
					+ user.getUUID().toString() + "' to '" + lastLogin + "': TimeStamp is the same.");
			return false;
		}

		// Database Query
		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + table + " SET lastLogin = ? WHERE uuid = ?");
			stmt.setLong(1, lastLogin);
			stmt.setString(2, user.getUUID().toString());

			stmt.execute();
			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Updated lastLogin of '" + user.getUUID().toString() + "' to '" + lastLogin + "'.");

			if (this.isCachingEnabled()) {
				user.setLastLogin(lastLogin);
			}

			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.WARNING, "Could not update lastLogin for user '"
					+ user.getLastKnownName() + "' to '" + lastLogin + "'/SQL: " + e.getMessage());
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
			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Did not update lastLogout of user '"
					+ user.getUUID().toString() + "' to '" + lastLogout + "': TimeStamp is the same.");
			return false;
		}

		// Database Query
		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + table + " SET lastLogout = ? WHERE uuid = ?");
			stmt.setLong(1, lastLogout);
			stmt.setString(2, user.getUUID().toString());

			stmt.execute();
			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Updated lastLogout of '" + user.getUUID().toString() + "' to '" + lastLogout + "'.");

			if (this.isCachingEnabled()) {
				user.setLastLogout(lastLogout);
			}

			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.WARNING, "Could not update lastLogout for user '"
					+ user.getLastKnownName() + "' to '" + lastLogout + "'/SQL: " + e.getMessage());
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
			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Did not update enabled state of user '"
					+ user.getUUID().toString() + ": State is already set to '" + enabled + "'.");
			return false;
		}

		// Database Query
		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + table + " SET enabled = ? WHERE uuid = ?");
			stmt.setBoolean(1, enabled);
			stmt.setString(2, user.getUUID().toString());

			stmt.execute();
			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Updated enabled state of '" + user.getUUID().toString() + "' to '" + enabled + "'.");

			if (this.isCachingEnabled()) {
				user.setEnabled(enabled);
			}

			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.WARNING, "Could not update enabled state for user '"
					+ user.getLastKnownName() + "' to '" + enabled + "'/SQL: " + e.getMessage());
			return false;
		}

	}
	
	public final boolean setLanguage(@Nonnull User user, @Nonnull Language language) {

		// Return if enabled state is the same
		if (user.getLanguage() == language) {
			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Did not update language of user '"
					+ user.getUUID().toString() + ": Language is already set to '" + language.toString() + "'.");
			return false;
		}

		// Database Query
		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + table + " SET language = ? WHERE uuid = ?");
			stmt.setString(1, language.toString());
			stmt.setString(2, user.getUUID().toString());

			stmt.execute();
			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Updated language of '" + user.getUUID().toString() + "' to '" + language.toString() + "'.");

			if (this.isCachingEnabled()) {
				user.setLanguage(language);
			}

			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.WARNING, "Could not update language for user '"
					+ user.getLastKnownName() + "' to '" + language.toString() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

}
