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
import at.peirleitner.core.util.user.MasterLicense;
import at.peirleitner.core.util.user.UserLicense;

/**
 * System to interact with {@link MasterLicense}s
 * 
 * @since 1.0.6
 * @author Markus Peirleitner (Rengobli)
 *
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
				"system.license.enable-caching", "false");
		
		// Load Data
		if(this.isCachingEnabled()) {
			this.getMasterLicensesFromDatabase();
		}

	}

	public final boolean isCachingEnabled() {
		return Core.getInstance().getSettingsManager().isSetting(Core.getInstance().getPluginName(),
				"system.license.enable-caching");
	}
	
	public final Collection<MasterLicense> getMasterLicenses() {
		return this.getCachedMasterLicenses().isEmpty() ? this.getMasterLicensesFromDatabase() : this.getCachedMasterLicenses();
	}
	
	public final Collection<UserLicense> getUserLicenses() {
		return this.getCachedUserLicenses().isEmpty() ? this.getUserLicensesFromDatabase() : this.getUserLicenses();
	}

	private final Collection<MasterLicense> getCachedMasterLicenses() {
		return cachedMasterLicenses;
	}

	public final MasterLicense getMasterLicense(@Nonnull int id) {
		return this.getCachedMasterLicenses().stream().filter(license -> license.getID() == id).findAny().orElse(null);
	}

	private final Collection<UserLicense> getCachedUserLicenses() {
		return cachedUserLicenses;
	}

	public final UserLicense getUserLicense(@Nonnull int id) {
		return this.getCachedUserLicenses().stream().filter(license -> license.getLicenseID() == id).findAny()
				.orElse(null);
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
