package at.peirleitner.core.system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import at.peirleitner.core.Core;
import at.peirleitner.core.command.local.CommandMotd;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.MOTD;
import at.peirleitner.core.util.database.TableType;
import at.peirleitner.core.util.user.CorePermission;

/**
 * System that allows interaction with the MessageOfTheDay
 * 
 * @since 1.0.4
 * @author Markus Peirleitner (Rengobli)
 * @see MOTD
 * @see CommandMotd
 * @see CorePermission#COMMAND_MOTD
 * @see Core#getTableMotd()
 */
public class MotdSystem {

	private MOTD cachedMotd;
	private final String table = TableType.MOTD.getTableName(true);

	public MotdSystem() {

		// Initialize
		this.cachedMotd = null;
		this.setDefaultMotd();

		// Load Data
		if (this.isCachingEnabled()) {
			this.getMotdFromDatabase();
		}

	}

	public final boolean isCachingEnabled() {
		return Core.getInstance().getSettingsManager().isSetting(Core.getInstance().getPluginName(),
				"manager.settings.cache-motd");
	}

	public final MOTD getMOTD() {
		return this.getFromCache() == null ? this.getMotdFromDatabase() : this.getFromCache();
	}

	public final List<MOTD> getMotds() {

		List<MOTD> motds = new ArrayList<>();

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + this.table);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				MOTD motd = this.getByResultSet(rs);
				motds.add(motd);
			}

//			Core.getInstance().log(this.getClass(), LogType.DEBUG,
//					"Returned " + motds.size() + " cached MOTDs on 'getMotds()'");
			return motds;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not get list of MOTDs from Database/SQL: " + e.getMessage());
			return null;
		}
	}

	private final boolean setDefaultMotd() {

		List<MOTD> motds = this.getMotds();
		
		if(!motds.isEmpty()) {
			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Did not attempt to set default motd since at least one does already exist.");
			return false;
		}
		
		MOTD motd = new MOTD("&fCore &7by &fRengobli &7- Default MOTD", "&7Website&8: &9www.peirleitner.at &7| Change with &e/motd", null, System.currentTimeMillis());
		
		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
					"INSERT INTO " + this.table + " (line1, line2, staff, changed) VALUES (?, ?, ?, ?);");
			stmt.setString(1, motd.getFirstLine());
			stmt.setString(2, motd.getSecondLine());
			stmt.setString(3, null);
			stmt.setLong(4, motd.getChanged());

			stmt.executeUpdate();
			
			if(this.isCachingEnabled()) {
				this.cachedMotd = motd;
			}

			Core.getInstance().log(this.getClass(), LogType.INFO, "Set current MOTD to default one");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not set default MOTD/SQL: " + e.getMessage());
			return false;
		}

	}

	private final MOTD getFromCache() {
		return this.cachedMotd;
	}

	private final MOTD getMotdFromDatabase() {

		if (Core.getInstance().getMySQL() == null || !Core.getInstance().getMySQL().isConnected()) {
			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Did not load MOTD from Database because no database connection has been established.");
			return null;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + this.table + " ORDER BY changed DESC LIMIT 1");
			ResultSet rs = stmt.executeQuery();
			rs.next();
			
			MOTD motd = this.getByResultSet(rs);

			if (this.isCachingEnabled()) {
				this.cachedMotd = motd;
			}

			return motd;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not get MOTD from Database/SQL: " + e.getMessage());
			return null;
		}

	}

	private final MOTD getByResultSet(@Nonnull ResultSet rs) throws SQLException {

		String line1 = rs.getString(1);
		String line2 = rs.getString(2);
		UUID staff = (rs.getString(3) == null ? null : UUID.fromString(rs.getString(3)));
		long changed = rs.getLong(4);

		return new MOTD(line1, line2, staff, changed);
	}

	public final boolean update(@Nullable UUID uuid, @Nonnull String text) {

		Core.getInstance().log(getClass(), LogType.DEBUG, "PASSED IN: " + text);
		
		final String split = "##";
		final String[] motd = text.split(split);
		
		if(motd.length < 2) {
			Core.getInstance().log(this.getClass(), LogType.WARNING, "Could not update MOTD: Exactly two lines are required. Please pass in a value that can be split by '" + split + "'. Example: '/motd &aFirst Line here" + split + "&bSecond Line here!'.");
			return false;
		}
		
		Core.getInstance().log(getClass(), LogType.DEBUG, "FIRST LINE: " + motd[0]);
		Core.getInstance().log(getClass(), LogType.DEBUG, "SECOND LINE: " + motd[1]);

		try {

			MOTD m = new MOTD(motd[0], motd[1], uuid, System.currentTimeMillis());

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
					"INSERT INTO " + this.table + " (line1, line2, staff, changed) VALUES (?, ?, ?, ?);");
			stmt.setString(1, m.getFirstLine());
			stmt.setString(2, m.getSecondLine());
			stmt.setString(3, (m.getStaff() == null ? null : m.getStaff().toString()));
			stmt.setLong(4, m.getChanged());

			stmt.executeUpdate();

			if (this.isCachingEnabled()) {
				this.cachedMotd = m;
			}

			Core.getInstance().log(this.getClass(), LogType.INFO, "User '"
					+ (uuid == null ? "CONSOLE" : uuid.toString()) + "' updated the MOTD to '" + m.toString() + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not update MOTD/SQL: " + e.getMessage());
			return false;
		}

	}

}
