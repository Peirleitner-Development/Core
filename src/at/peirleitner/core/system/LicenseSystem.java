package at.peirleitner.core.system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import at.peirleitner.core.Core;
import at.peirleitner.core.command.local.CommandLicense;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.TableType;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.local.GUI;
import at.peirleitner.core.util.user.MasterLicense;
import at.peirleitner.core.util.user.UserLicense;

/**
 * System to interact with Licenses
 * 
 * @since 1.0.6
 * @author Markus Peirleitner (Rengobli)
 * @see UserLicense
 * @see MasterLicense
 */
public class LicenseSystem {

	private final String tableMaster = TableType.LICENSES_MASTER.getTableName(true);
	private final String tableUser = TableType.LICENSES_USER.getTableName(true);

	private final Collection<MasterLicense> cachedMasterLicenses;
	private final Collection<UserLicense> cachedUserLicenses;

	public LicenseSystem() {

		// Initialize
		this.cachedMasterLicenses = new ArrayList<>();
		this.cachedUserLicenses = new ArrayList<>();

		Core.getInstance().getSettingsManager().registerSetting(Core.getInstance().getPluginName(),
				"system.license.enable-caching", "true");
		Core.getInstance().getSettingsManager().registerSetting(Core.getInstance().getPluginName(),
				"system.license.display-licenses-in-gui", "true");
		
		// Load Data
		if (this.isCachingEnabled()) {
			this.getMasterLicensesFromDatabase();
			this.getUserLicensesFromDatabase();
		}

	}

	public final boolean isCachingEnabled() {
		return Core.getInstance().getSettingsManager().isSetting(Core.getInstance().getPluginName(),
				"system.license.enable-caching");
	}
	
	/**
	 * 
	 * @return If Licenses should be displayed inside a {@link GUI}
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 * @see CommandLicense
	 * @apiNote Otherwise they will be displayed in the Chat
	 */
	public final boolean isDisplayInGUI() {
		return Core.getInstance().getSettingsManager().isSetting(Core.getInstance().getPluginName(),
				"system.license.display-licenses-in-gui");
	}

	public final Collection<MasterLicense> getMasterLicenses() {
		return this.getCachedMasterLicenses().isEmpty() ? this.getMasterLicensesFromDatabase()
				: this.getCachedMasterLicenses();
	}

	public final Collection<UserLicense> getUserLicenses() {
		return this.getCachedUserLicenses().isEmpty() ? this.getUserLicensesFromDatabase() : this.getCachedUserLicenses();
	}

	private final Collection<MasterLicense> getCachedMasterLicenses() {
		return cachedMasterLicenses;
	}

	public final MasterLicense getMasterLicense(@Nonnull int id) {
		return this.isCachingEnabled() ? this.getMasterLicenseFromCache(id) : this.getMasterLicenseFromDatabase(id);
	}

	private final MasterLicense getMasterLicenseFromCache(@Nonnull int id) {
		return this.getCachedMasterLicenses().stream().filter(license -> license.getID() == id).findAny().orElse(null);
	}

	private final MasterLicense getMasterLicenseFromDatabase(@Nonnull int id) {

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + this.tableMaster + " WHERE id = ?");
			stmt.setInt(1, id);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				MasterLicense ml = this.getMasterLicenseByResultSet(rs);

				if (this.isCachingEnabled()) {
					this.cachedMasterLicenses.add(ml);
				}

				return ml;

			} else {
				// No License found
				return null;
			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get MasterLicense from Database with provided ID '" + id + "'/SQL:" + e.getMessage());
			return null;
		}

	}

	private final Collection<UserLicense> getCachedUserLicenses() {
		return cachedUserLicenses;
	}

	public final UserLicense getUserLicense(@Nonnull int id) {
		return this.getCachedUserLicenses().stream().filter(license -> license.getLicenseID() == id).findAny()
				.orElse(null);
	}

	public final Collection<UserLicense> getLicenses(@Nonnull UUID uuid) {

		Collection<UserLicense> licenses = new ArrayList<>();

		if (this.isCachingEnabled()) {

			for (UserLicense ul : this.getUserLicenses()) {
				if (ul.getOwner().equals(uuid)) {
					licenses.add(ul);
				}
			}

			return licenses;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + this.tableUser + " WHERE uuid = ?");
			stmt.setString(1, uuid.toString());

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				UserLicense ul = this.getUserLicenseByResultSet(rs);
				licenses.add(ul);

			}

			return licenses;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get Licenses for User '" + uuid.toString() + "'/SQL: " + e.getMessage());
			return null;
		}

	}

	public final boolean hasLicense(@Nonnull UUID uuid, @Nonnull MasterLicense masterLicense) {
		return this.getLicense(uuid, masterLicense) != null ? true : false;
	}

	public final boolean hasActiveLicense(@Nonnull UUID uuid, @Nonnull MasterLicense masterLicense) {
		return this.hasLicense(uuid, masterLicense) && this.getLicense(uuid, masterLicense).isValid() ? true : false;
	}

	public final UserLicense getLicense(@Nonnull UUID uuid, @Nonnull MasterLicense masterLicense) {

		Collection<UserLicense> licenses = this.getLicenses(uuid);

		for (UserLicense ul : licenses) {

			if (ul.getLicenseID() == masterLicense.getID()) {
				return ul;
			}

		}

		return null;
	}

	public final boolean grantLicense(@Nonnull UUID uuid, @Nonnull MasterLicense masterLicense, @Nonnull int hours) {

		if (this.hasActiveLicense(uuid, masterLicense)) {
			Core.getInstance().log(getClass(), LogType.WARNING, "Did not grant '" + uuid.toString() + "' the license '"
					+ masterLicense.toString() + "' because it has already been issued.");
			return false;
		}

		try {

			PreparedStatement stmt = null;
			long expire = -1;

			if (hours > 0) {
				expire = System.currentTimeMillis() + (1000L * 60 * 60 * hours);
			}

			if (this.hasLicense(uuid, masterLicense)) {
				// Update existing
				stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
						"UPDATE " + this.tableUser + " SET issued = ?, expire = ? WHERE uuid = ? AND license = ?");
				stmt.setLong(1, System.currentTimeMillis());
				stmt.setLong(2, expire);
				stmt.setString(3, uuid.toString());
				stmt.setInt(4, masterLicense.getID());
			} else {
				// Create new
				stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
						"INSERT INTO " + this.tableUser + " (uuid, license, issued, expire) VALUES (?, ?, ?, ?);");
				stmt.setString(1, uuid.toString());
				stmt.setInt(2, masterLicense.getID());
				stmt.setLong(3, System.currentTimeMillis());
				stmt.setLong(4, expire);
			}

			stmt.executeUpdate();
			
			if(this.isCachingEnabled()) {
				this.getUserLicensesFromDatabase();
			}
			
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not grant the License '" + masterLicense.toString()
					+ "' to the User '" + uuid.toString() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	/**
	 * 
	 * @param saveType
	 * @param name
	 * @return If a {@link MasterLicense} with the given SaveType and Name does
	 *         already exist
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean isLicense(@Nonnull SaveType saveType, @Nonnull String name) {

		if (this.isCachingEnabled()) {

			for (MasterLicense ml : this.getMasterLicenses()) {
				if (ml.getSaveTypeID() == saveType.getID() && ml.getName().equalsIgnoreCase(name)) {
					return true;
				}
			}

			return false;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + this.tableUser + " WHERE saveType = ? AND name = ?");
			stmt.setInt(1, saveType.getID());
			stmt.setString(2, name);

			ResultSet rs = stmt.executeQuery();
			return rs.next();

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not check if a MasterLicense for SaveType '" + saveType.toString() + "' and name '" + name
							+ "' exists inside the Database/SQL: " + e.getMessage());
			return false;
		}

	}

	/**
	 * 
	 * @return Default name for {@link MasterLicense#getIconName()}
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getDefaultIconName() {
		return "PAPER";
	}

	/**
	 * Create a new MasterLicense
	 * 
	 * @param saveType
	 * @param name
	 * @param expire
	 * @param iconName
	 * @return If a new Master License has been created
	 * @since 1.0.6
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean createMasterLicense(@Nonnull SaveType saveType, @Nonnull String name, @Nonnull long expire,
			@Nullable String iconName) {

		if (this.isLicense(saveType, iconName)) {
			Core.getInstance().log(getClass(), LogType.WARNING,
					"Did not create new Master License because one does already exist for the SaveType '"
							+ saveType.toString() + "' and name '" + name + "'.");
			return false;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
					"INSERT INTO " + this.tableMaster + " (saveType, name, expire, iconName) VALUES (?, ?, ?, ?);",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, saveType.getID());
			stmt.setString(2, name);
			stmt.setLong(3, expire);
			stmt.setString(4, iconName == null ? this.getDefaultIconName() : iconName);

			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			rs.next();

			MasterLicense ml = new MasterLicense(rs.getInt(1), saveType.getID(), name, System.currentTimeMillis(),
					expire, iconName);

			if (this.isCachingEnabled()) {
				this.getCachedMasterLicenses().add(ml);
			}

			return true;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not create new Master License/SQL: " + e.getMessage());
			return false;
		}

	}

	public final boolean setMasterLicenseToExpire(@Nonnull int masterLicenseID) {

		MasterLicense ml = this.getMasterLicense(masterLicenseID);

		if (ml == null) {
			Core.getInstance().log(getClass(), LogType.DEBUG, "Could not expire Master License with ID '"
					+ masterLicenseID + "' because no License with the given ID could be found.");
			return false;
		}

		if (!ml.isValid()) {
			Core.getInstance().log(getClass(), LogType.DEBUG,
					"Could not expire Master License with ID '" + masterLicenseID + "' because it's already expired.");
			return false;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + this.tableMaster + " SET expire = ? WHERE id = ?");
			stmt.setLong(1, System.currentTimeMillis());
			stmt.setInt(2, masterLicenseID);

			stmt.executeUpdate();

			if (this.isCachingEnabled()) {
				ml.setExpire(System.currentTimeMillis());
			}

			return true;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not expire Master License '" + masterLicenseID + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	public final boolean setUserLicenseToExpire(@Nonnull UUID uuid, @Nonnull MasterLicense masterLicense) {

		if (!this.hasLicense(uuid, masterLicense)) {
			Core.getInstance().log(getClass(), LogType.WARNING,
					"Did not expire User License '" + masterLicense.toString() + "' for User '" + uuid.toString()
							+ "': User does not have an active one.");
			return false;
		}

		UserLicense ul = this.getUserLicense(masterLicense.getID());

		if (!ul.isValid()) {
			Core.getInstance().log(getClass(), LogType.WARNING, "Did not expire User License '"
					+ masterLicense.toString() + "' for User '" + uuid.toString() + "': Already expired.");
			return false;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + this.tableUser + " SET expire = ? WHERE license = ?");
			stmt.setLong(1, System.currentTimeMillis());
			stmt.setInt(2, masterLicense.getID());

			stmt.executeUpdate();

			if (this.isCachingEnabled()) {
				ul.setExpire(System.currentTimeMillis());
			}

			return true;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not expire Master License '" + masterLicense.toString() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	public final boolean setMasterLicenseToPermanent(@Nonnull int masterLicenseID) {

		MasterLicense ml = this.getMasterLicense(masterLicenseID);

		if (ml == null) {
			Core.getInstance().log(getClass(), LogType.DEBUG, "Could not set Master License with ID '" + masterLicenseID
					+ "' to permanent because no License with the given ID could be found.");
			return false;
		}

		if (ml.isPermanent()) {
			Core.getInstance().log(getClass(), LogType.DEBUG, "Could not set Master License with ID '" + masterLicenseID
					+ "' to permanent because it's already permanent.");
			return false;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + this.tableMaster + " SET expire = ? WHERE id = ?");
			stmt.setLong(1, -1);
			stmt.setInt(2, masterLicenseID);

			stmt.executeUpdate();

			if (this.isCachingEnabled()) {
				ml.setExpire(-1);
			}

			return true;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not set Master License '" + masterLicenseID + "' to permanent/SQL: " + e.getMessage());
			return false;
		}

	}

	private final Collection<MasterLicense> getMasterLicensesFromDatabase() {

		Collection<MasterLicense> licenses = new ArrayList<>();

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + this.tableMaster);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				MasterLicense license = this.getMasterLicenseByResultSet(rs);
				licenses.add(license);

			}

			if (this.isCachingEnabled()) {
				this.cachedMasterLicenses.clear();
				this.cachedMasterLicenses.addAll(licenses);
			}

			return licenses;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not get Master Licenses/SQL: " + e.getMessage());
			return null;
		}

	}

	private final Collection<UserLicense> getUserLicensesFromDatabase() {

		Collection<UserLicense> licenses = new ArrayList<>();

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + this.tableUser);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				UserLicense license = this.getUserLicenseByResultSet(rs);
				licenses.add(license);

			}

			if (this.isCachingEnabled()) {
				this.cachedUserLicenses.clear();
				this.cachedUserLicenses.addAll(licenses);
			}

			return licenses;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not get User Licenses/SQL: " + e.getMessage());
			return null;
		}

	}

	private final MasterLicense getMasterLicenseByResultSet(@Nonnull ResultSet rs) throws SQLException {

		int id = rs.getInt(1);
		int saveTypeID = rs.getInt(2);
		String name = rs.getString(3);
		long created = rs.getLong(4);
		long expire = rs.getLong(5);
		String iconName = rs.getString(6);

		return new MasterLicense(id, saveTypeID, name, created, expire, iconName);
	}

	private final UserLicense getUserLicenseByResultSet(@Nonnull ResultSet rs) throws SQLException {

		UUID owner = UUID.fromString(rs.getString(1));
		int licenseID = rs.getInt(2);
		long issued = rs.getLong(3);
		long expire = rs.getLong(4);

		return new UserLicense(owner, licenseID, issued, expire);
	}

}
