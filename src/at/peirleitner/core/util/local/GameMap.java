package at.peirleitner.core.util.local;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.CustomLocation;
import at.peirleitner.core.util.GlobalUtils;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.database.TableType;
import net.md_5.bungee.api.ChatColor;

/**
 * Wrapper for {@link GameMapData}
 * 
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class GameMap implements GameMapData {

	private int id;
	private String name;
	private SaveType saveType;
	private String iconName;
	private UUID creator;
	private Collection<UUID> contributors;
	private GameMapState state;
	private Collection<CustomLocation> spawns;
	private boolean isTeams;

	public GameMap() {
	}

	public GameMap(int id, String name, SaveType saveType, String iconName, UUID creator, Collection<UUID> contributors,
			GameMapState state, Collection<CustomLocation> spawns, boolean isTeams) {
		this.id = id;
		this.name = name;
		this.saveType = saveType;
		this.iconName = iconName;
		this.creator = creator;
		this.contributors = contributors;
		this.state = state;
		this.spawns = spawns;
		this.isTeams = isTeams;
	}

	public GameMap(String name, SaveType saveType, String iconName, UUID creator, Collection<UUID> contributors,
			GameMapState state, Collection<CustomLocation> spawns, boolean isTeams) {
		this.name = name;
		this.saveType = saveType;
		this.iconName = iconName;
		this.creator = creator;
		this.contributors = contributors;
		this.state = state;
		this.spawns = spawns;
		this.isTeams = isTeams;
	}

	@Override
	public final int getID() {
		return id;
	}

	public final void setID(int id) {
		this.id = id;
	}

	@Override
	public final String getName() {
		return name;
	}

	public final boolean setName(@Nonnull String name) {

		name = ChatColor.stripColor(name);

		if (this.getName().equalsIgnoreCase(name))
			return false;

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + TableType.MAPS.getTableName(true) + " SET name = ? WHERE id = ?");
			stmt.setString(1, name);
			stmt.setInt(2, this.getID());

			String old = this.getName();

			stmt.executeUpdate();
			this.name = name;

			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Updated Name of Map '" + this.getID() + "' from '" + old + "' to '" + name + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not set Name of Map '" + this.getID() + "' to '" + name + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	@Override
	public final SaveType getSaveType() {
		return saveType;
	}

	public final boolean setSaveType(@Nonnull SaveType saveType) {

		if (this.getSaveType() == saveType)
			return false;

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + TableType.MAPS.getTableName(true) + " SET saveType = ? WHERE id = ?");
			stmt.setInt(1, saveType.getID());
			stmt.setInt(2, this.getID());

			SaveType old = this.getSaveType();

			stmt.executeUpdate();
			this.saveType = saveType;

			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Updated SaveType of Map '" + this.getID()
					+ "' from '" + old.getName() + "' to '" + saveType.getName() + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not set SaveType of Map '" + this.getID()
					+ "' to '" + saveType.getName() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	@Override
	public final String getIconName() {
		return iconName;
	}

	public final boolean setIcon(@Nonnull String iconName) {

		if (this.getIconName().equalsIgnoreCase(iconName))
			return false;

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + TableType.MAPS.getTableName(true) + " SET icon = ? WHERE id = ?");
			stmt.setString(1, iconName);
			stmt.setInt(2, this.getID());

			String old = this.getIconName();

			stmt.executeUpdate();
			this.iconName = iconName;

			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Updated Icon of Map '" + this.getID() + "' from '" + old + "' to '" + iconName + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not set Icon of Map '" + this.getID() + "' to '" + iconName + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	public final String getDefaultIconName() {
		return "PAPER";
	}

	@Override
	public final UUID getCreator() {
		return creator;
	}

	public final boolean setCreator(@Nonnull UUID creator) {

		if (this.getCreator().equals(creator))
			return false;

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + TableType.MAPS.getTableName(true) + " SET creator = ? WHERE id = ?");
			stmt.setString(1, creator.toString());
			stmt.setInt(2, this.getID());

			String old = this.getCreator().toString();

			stmt.executeUpdate();
			this.creator = creator;

			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Updated Creator of Map '" + this.getID()
					+ "' from '" + old + "' to '" + creator.toString() + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not set Creator of Map '" + this.getID()
					+ "' to '" + creator.toString() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	/**
	 * 
	 * @param uuid
	 * @return
	 * @since 1.0.3
	 */
	public final boolean isCreator(@Nonnull UUID uuid) {
		return this.getCreator().equals(uuid);
	}

	@Override
	public final Collection<UUID> getContributors() {
		return contributors;
	}

	public final boolean setContributors(@Nonnull Set<UUID> contributors) {

		if (this.hasContributors() && this.getContributors() == contributors)
			return false;

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
					"UPDATE " + TableType.MAPS.getTableName(true) + " SET contributors = ? WHERE id = ?");
			stmt.setString(1, GlobalUtils.getUuidString(contributors, ";"));
			stmt.setInt(2, this.getID());

			String old = GlobalUtils.getUuidString(this.getContributors(), ";");

			stmt.executeUpdate();
			this.contributors = contributors;

			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Updated Contributors of Map '" + this.getID()
					+ "' from '" + old + "' to '" + GlobalUtils.getUuidString(contributors, ";") + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not set Contributors of Map '" + this.getID()
					+ "' to '" + GlobalUtils.getUuidString(contributors, ";") + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	public final boolean hasContributors() {
		return this.getContributors() == null || this.getContributors().isEmpty() ? false : true;
	}

	/**
	 * 
	 * @param uuid
	 * @return
	 * @since 1.0.3
	 */
	public final boolean isContributor(@Nonnull UUID uuid) {
		return !this.hasContributors() ? false : this.getContributors().contains(uuid);
	}

	@Override
	public final GameMapState getState() {
		return state;
	}

	/**
	 * Update the State of a Map
	 * 
	 * @param map   - Map
	 * @param state - New State
	 * @return If the State has been updated
	 * @since 1.0.3
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final boolean setState(@Nonnull GameMapState state) {

		if (this.getState() == state)
			return false;

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + TableType.MAPS.getTableName(true) + " SET state = ? WHERE id = ?");
			stmt.setString(1, state.toString());
			stmt.setInt(2, this.getID());

			GameMapState old = this.getState();

			stmt.executeUpdate();
			this.state = state;

			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Updated State of Map '" + this.getID() + "' from '"
					+ old.toString() + "' to '" + state.toString() + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not set State of Map '" + this.getID()
					+ "' to '" + state.toString() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	/**
	 * 
	 * @param state
	 * @return
	 * @since 1.0.3
	 */
	public final boolean isState(@Nonnull GameMapState state) {
		return this.state == state;
	}

	@Override
	public final Collection<CustomLocation> getSpawns() {
		return spawns;
	}

	public final boolean setSpawns(@Nullable Set<CustomLocation> spawns) {

//		if (map.hasSpawns() && map.getSpawns() == spawns)
//			return false;

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + TableType.MAPS.getTableName(true) + " SET spawns = ? WHERE id = ?");
			stmt.setString(1, spawns == null ? null : GlobalUtils.getCustomLocationStringFromList(spawns));
			stmt.setInt(2, this.getID());

			stmt.executeUpdate();
			this.spawns = spawns;

			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Updated Spawns of Map '" + this.getID() + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not set Spawns of Map '" + this.getID() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	public final boolean hasSpawns() {
		return this.getSpawns() == null || this.getSpawns().isEmpty() ? false : true;
	}

	@Override
	public final boolean isTeams() {
		return isTeams;
	}

	public final boolean setTeams(@Nonnull boolean teams) {

		if (this.isTeams() && teams || !this.isTeams() && !teams) {
//			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Did not update Teams of Map '" + map.getID() + "' because the value would be identical.");
			return false;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + TableType.MAPS.getTableName(true) + " SET teams = ? WHERE id = ?");
			stmt.setBoolean(1, teams);
			stmt.setInt(2, this.getID());

			stmt.executeUpdate();

			boolean old = this.isTeams();
			this.isTeams = teams;

			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Updated Teams of Map '" + this.getID() + " from '" + old + "' to '" + teams + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not update Teams of Map '" + this.getID() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	@Override
	public String toString() {
		return "GameMap [id=" + id + ", name=" + name + ", saveType=" + saveType + ", iconName=" + iconName
				+ ", creator=" + creator + ", contributors=" + contributors + ", state=" + state + ", spawns=" + spawns
				+ ", isTeams=" + isTeams + "]";
	}

}
