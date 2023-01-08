package at.peirleitner.core.system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.CoreSystem;
import at.peirleitner.core.util.GlobalUtils;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.database.TableType;
import at.peirleitner.core.util.user.CooldownInfo;
import at.peirleitner.core.util.user.User;

/**
 * This System is used to save last and next usage of actions that require
 * cooldowns.
 * 
 * @since 1.0.15
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class CooldownSystem implements CoreSystem {

	public CooldownSystem() {
		this.createTable();
	}

	private final boolean hasRecord(@Nonnull UUID uuid, @Nonnull String metadata, @Nonnull int saveType) {

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("SELECT * FROM "
					+ this.getTableType().getTableName(true) + " WHERE uuid = ? AND metadata = ? AND saveType = ?");
			stmt.setString(1, uuid.toString());
			stmt.setString(2, metadata);
			stmt.setInt(3, saveType);

			ResultSet rs = stmt.executeQuery();

			return rs.next();

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not check if a Record for User '" + uuid.toString() + "' with Metadata '" + metadata
							+ "' on SaveType '" + saveType + "' exists/SQL: " + e.getMessage());
			return false;
		}

	}
	
	/**
	 * 
	 * @param uuid
	 * @param metadata
	 * @param saveType
	 * @param seconds
	 * @return
	 * @since 1.0.19
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean addCooldown(@Nonnull UUID uuid, @Nonnull String metadata, @Nonnull int saveType, @Nonnull int seconds) {
		
		long expiration = System.currentTimeMillis() + (1000L * seconds);
		return this.addCooldown(uuid, metadata, saveType, expiration);
		
	}

	public final boolean addCooldown(@Nonnull UUID uuid, @Nonnull String metadata, @Nonnull int saveType,
			@Nonnull long nextUsage) {

		if (this.hasCooldown(uuid, metadata, saveType)) {
			Core.getInstance().log(getClass(), LogType.WARNING, "Not adding Cooldown for User '" + uuid.toString()
					+ "' on Metadata '" + metadata + "': Already has active Cooldown.");
			return false;
		}

		try {

			PreparedStatement stmt = null;

			if (this.hasRecord(uuid, metadata, saveType)) {

				stmt = Core.getInstance().getMySQL().getConnection()
						.prepareStatement("UPDATE " + this.getTableType().getTableName(true)
								+ " SET lastUsage = ?, nextUsage = ? WHERE uuid = ? AND metadata = ? AND saveType = ?");
				stmt.setLong(1, System.currentTimeMillis());
				stmt.setLong(2, nextUsage);
				stmt.setString(3, uuid.toString());
				stmt.setString(4, metadata);
				stmt.setInt(5, saveType);

			} else {

				stmt = Core.getInstance().getMySQL().getConnection()
						.prepareStatement("INSERT INTO " + this.getTableType().getTableName(true)
								+ " (uuid, metadata, lastUsage, nextUsage, saveType) VALUES (?, ?, ?, ?, ?);");
				stmt.setString(1, uuid.toString());
				stmt.setString(2, metadata);
				stmt.setLong(3, System.currentTimeMillis());
				stmt.setLong(4, nextUsage);
				stmt.setInt(5, saveType);

			}

			int update = stmt.executeUpdate();

			if (update < 0) {
				Core.getInstance().log(getClass(), LogType.WARNING,
						"Could not add Cooldown for User '" + uuid.toString() + "' on Metadata '" + metadata
								+ ": SQL Update returned less than one updated row");
				return false;
			} else {
				return true;
			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not add Cooldown for User '" + uuid.toString()
					+ "' on Metadata '" + metadata + "/SQL: " + e.getMessage());
			return false;
		}

	}

	public final boolean removeCooldown(@Nonnull UUID uuid, @Nonnull String metadata, @Nonnull int saveType) {

		if (!this.hasCooldown(uuid, metadata, saveType)) {
			Core.getInstance().log(getClass(), LogType.WARNING, "Could not remove Cooldown for User '" + uuid.toString()
					+ "' on Metadata '" + metadata + "' with SaveType '" + saveType + "': No Cooldown exists.");
			return false;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("DELETE FROM "
					+ this.getTableType().getTableName(true) + " WHERE uuid = ? AND metadata = ? AND saveType = ?");
			stmt.setString(1, uuid.toString());
			stmt.setString(2, metadata);
			stmt.setInt(3, saveType);

			int update = stmt.executeUpdate();

			if (update < 0) {
				Core.getInstance().log(getClass(), LogType.WARNING,
						"Could not remove Cooldown for User '" + uuid.toString() + "' on Metadata '" + metadata
								+ ": SQL Update returned less than one updated row");
				return false;
			} else {
				return true;
			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.WARNING, "Could not remove Cooldown for User '" + uuid.toString()
					+ "' on Metadata '" + metadata + "' with SaveType '" + saveType + "'/SQL: " + e.getMessage());
			return false;
		}

	}
	
	/**
	 * 
	 * @param uuid
	 * @param metadata
	 * @param saveType
	 * @param message
	 * @return
	 * @since 1.0.19
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean hasCooldown(@Nonnull UUID uuid, @Nonnull String metadata, @Nonnull int saveType, @Nonnull boolean message) {
		
		boolean has = this.hasCooldown(uuid, metadata, saveType);
		
		if(message && has) {
			
			Collection<CooldownInfo> cooldowns = this.getActiveCooldowns(uuid, Core.getInstance().getSaveTypeByID(saveType));
			
			for(CooldownInfo ci : cooldowns) {
				if(ci.getMetadata().equals(metadata)) {
					
					long nextUsage = ci.getNextUsage();
					Core.getInstance().getUserSystem().getUser(uuid).sendMessage(Core.getInstance().getPluginName(), "system.cooldown.has-cooldown-main-message", Arrays.asList(GlobalUtils.getFormatedDate(nextUsage)), true);
					
				}
			}
			
		}
		
		return has;
	}

	/**
	 * Check if the given {@link User} has an active Cooldown for the given Metadata
	 * on the given {@link SaveType}
	 * 
	 * @param uuid     - UUID of the User
	 * @param metadata - Metadata
	 * @param saveType - SaveType ID
	 * @return If an active Cooldown is available
	 * @since 1.0.15
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote This will return <code>true</code> if an {@link SQLException}
	 *          occurs.
	 */
	public final boolean hasCooldown(@Nonnull UUID uuid, @Nonnull String metadata, @Nonnull int saveType) {

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT nextUsage FROM " + this.getTableType().getTableName(true)
							+ " WHERE uuid = ? AND metadata = ? AND saveType = ?");
			stmt.setString(1, uuid.toString());
			stmt.setString(2, metadata);
			stmt.setInt(3, saveType);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				long nextUsage = rs.getLong(1);
				return System.currentTimeMillis() >= nextUsage ? false : true;

			} else {
				// No record exists, no Cooldown.
				return false;
			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not check if the User '" + uuid.toString()
					+ "' has a Cooldown for '" + metadata + "'/SQL: " + e.getMessage());
			return true;
		}

	}

	public final Collection<CooldownInfo> getActiveCooldowns(@Nonnull UUID uuid) {

		List<CooldownInfo> cooldowns = new ArrayList<>();

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT metadata, lastUsage, nextUsage, saveType FROM "
							+ this.getTableType().getTableName(true) + " WHERE uuid = ? AND nextUsage > ?");
			stmt.setString(1, uuid.toString());
			stmt.setLong(2, System.currentTimeMillis());

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				
				String metadata = rs.getString(1);
				long lastUsage = rs.getLong(2);
				long nextUsage = rs.getLong(3);
				SaveType saveType = Core.getInstance().getSaveTypeByID(rs.getInt(4));
				
				CooldownInfo ci = new CooldownInfo(metadata, lastUsage, nextUsage, saveType);
				cooldowns.add(ci);

			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get active Cooldowns for User '" + uuid.toString() + "'/SQL: " + e.getMessage());
			return null;
		}

		return cooldowns;
	}
	
	public final Collection<CooldownInfo> getActiveCooldowns(@Nonnull UUID uuid, @Nonnull SaveType saveType) {

		List<CooldownInfo> cooldowns = new ArrayList<>();

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT metadata, lastUsage, nextUsage FROM "
							+ this.getTableType().getTableName(true) + " WHERE uuid = ? AND nextUsage > ? AND saveType = ?");
			stmt.setString(1, uuid.toString());
			stmt.setLong(2, System.currentTimeMillis());
			stmt.setInt(3, saveType.getID());

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				
				String metadata = rs.getString(1);
				long lastUsage = rs.getLong(2);
				long nextUsage = rs.getLong(3);
				
				CooldownInfo ci = new CooldownInfo(metadata, lastUsage, nextUsage, saveType);
				cooldowns.add(ci);

			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get active Cooldowns for User '" + uuid.toString() + "'/SQL: " + e.getMessage());
			return null;
		}

		return cooldowns;
	}

	@Override
	public final void createTable() {

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getTableType().getTableName(true) + " ("
							+ "uuid CHAR(36) NOT NULL, " + "metadata VARCHAR(100) NOT NULL, "
							+ "lastUsage BIGINT(255) NOT NULL DEFAULT '" + System.currentTimeMillis() + "', "
							+ "nextUsage BIGINT(255) NOT NULL, " + "saveType INT NOT NULL, "
							+ "PRIMARY KEY (uuid, metadata, saveType), " + "FOREIGN KEY (saveType) REFERENCES "
							+ TableType.SAVE_TYPE.getTableName(true) + "(id)" + ");");
			stmt.execute();

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not create Table for CooldownSystem/SQL: " + e.getMessage());
		}

	}

	@Override
	public final TableType getTableType() {
		return TableType.COOLDOWNS;
	}

}
