package at.peirleitner.core.system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
import at.peirleitner.core.manager.SettingsManager;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.user.CorePermission;

/**
 * System to interact with maintenance
 * 
 * @since 1.0.5
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class MaintenanceSystem {

	private final String key = "manager.settings.maintenance";
	private final String table = Core.getInstance().getTableMaintenance();
	private Collection<UUID> whitelist;

	public MaintenanceSystem() {

		// Initialize
		this.whitelist = new ArrayList<>();

		// Load Data
		this.loadFromDatabase();

	}

	/**
	 * 
	 * @return Key for {@link SettingsManager#getSetting(String, String)}
	 * @since 1.0.5
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getKey() {
		return this.key;
	}

	public final boolean isWhitelisted(@Nonnull UUID uuid) {
		return this.getWhitelisted().contains(uuid);
	}

	public final Collection<UUID> getWhitelisted() {
		return this.whitelist;
	}

	/**
	 * 
	 * @return If maintenance mode is currently active. While active, only members
	 *         that are listed inside {@link Core#getTableMaintenance()} are able to
	 *         connect.
	 * @since 1.0.5
	 * @author Markus Peirleitner (Rengobli)
	 * @see CorePermission#BYPASS_MAINTENANCE
	 */
	public final boolean isMaintenance() {
		return Boolean.valueOf(
						Core.getInstance().getSettingsManager().getSetting(Core.getInstance().getPluginName(), "manager.settings.maintenance"));
	}

	public final boolean toggleMaintenance() {
		return this.setMaintenance(this.isMaintenance() ? false : true);
	}

	public final boolean setMaintenance(@Nonnull boolean value) {

		if (this.isMaintenance() == value) {
			Core.getInstance().log(getClass(), LogType.DEBUG,
					"Did not update maintenance state because it is already set to '" + value + "'.");
			return false;
		}

		return Core.getInstance().getSettingsManager().setSetting(Core.getInstance().getPluginName(), this.getKey(),
				"" + value);
	}

	public final boolean isListedInDatabase(@Nonnull UUID uuid) {

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + this.table + " WHERE uuid = ?");
			stmt.setString(1, uuid.toString());
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return true;
			} else {
				// Not listed
				return false;
			}

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not check if the User '" + uuid.toString()
					+ "' is on the maintenace list/SQL: " + e.getMessage());
			return false;
		}

	}

	private final void loadFromDatabase() {
		this.whitelist.clear();
		this.whitelist.addAll(this.getFromDatabase());
	}

	private final Collection<UUID> getFromDatabase() {

		List<UUID> list = new ArrayList<>();

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + this.table);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				list.add(UUID.fromString(rs.getString(1)));
			}

			return list;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not load whitelisted players of maintenace list/SQL: " + e.getMessage());
			return null;
		}

	}

	public final boolean addToWhitelist(@Nonnull UUID uuid) {

		if (this.isListedInDatabase(uuid)) {
			Core.getInstance().log(getClass(), LogType.DEBUG,
					"Did not add '" + uuid.toString() + "' to whitelist: Already listed.");
			return false;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("INSERT INTO " + this.table + " (uuid) VALUES (?);");
			stmt.setString(1, uuid.toString());

			stmt.executeUpdate();
			this.whitelist.add(uuid);

			Core.getInstance().log(getClass(), LogType.DEBUG, "Added '" + uuid.toString() + "' to maintenance list");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not add '" + uuid.toString() + "' to whitelist/SQL: " + e.getMessage());
			return false;
		}

	}

	public final boolean removeFromWhitelist(@Nonnull UUID uuid) {

		if (!this.isListedInDatabase(uuid)) {
			Core.getInstance().log(getClass(), LogType.DEBUG,
					"Did not remove '" + uuid.toString() + "' to whitelist: Not listed.");
			return false;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("DELETE FROM " + this.table + " WHERE uuid = ?");
			stmt.setString(1, uuid.toString());

			stmt.executeUpdate();
			this.whitelist.remove(uuid);

			Core.getInstance().log(getClass(), LogType.DEBUG,
					"Removed '" + uuid.toString() + "' from maintenance list");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not remove '" + uuid.toString() + "' from whitelist/SQL: " + e.getMessage());
			return false;
		}

	}

}
