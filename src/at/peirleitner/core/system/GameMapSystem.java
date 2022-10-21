package at.peirleitner.core.system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.CustomLocation;
import at.peirleitner.core.util.GlobalUtils;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.TableType;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.local.GameMap;
import at.peirleitner.core.util.local.GameMapState;
import at.peirleitner.core.util.user.User;
import net.md_5.bungee.api.ChatColor;

/**
 * System used to interact with {@link GameMap}s
 * 
 * @since 1.0.3
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class GameMapSystem {

	private final String table = TableType.MAPS.getTableName(true);
	private Collection<GameMap> cachedMaps;

	public GameMapSystem() {

		// Initialize
		this.cachedMaps = new ArrayList<>();

		// Load Data
		this.loadMaps();

	}

	private final void loadMaps() {

		if (!this.isMapCachingEnabled()) {
			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Did not load Maps because caching has been disabled.");
			return;
		}

		Collection<GameMap> maps = this.getMaps();
		Core.getInstance().log(this.getClass(), LogType.INFO, "Cached " + maps.size() + " Maps from Database.");

	}

	public final boolean isMapCachingEnabled() {
		return Core.getInstance().getSettingsManager().isSetting(Core.getInstance().getPluginName(),
				"manager.settings.cache-game-maps");
	}

	public final GameMap getMap(@Nonnull int id) {

		GameMap map = this.cachedMaps.stream().filter(m -> m.getID() == id).findAny().orElse(null);

		// Return cached value if available
		if (map != null) {
			return map;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + this.table + " WHERE id = ?");
			stmt.setInt(1, id);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				map = this.getByResultSet(rs);
				this.cache(map);

				return map;

			} else {
				return null;
			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get Map with ID '" + id + "' from Database/SQL: " + e.getMessage());
			return null;
		}

	}

	public final boolean isMap(@Nonnull int id) {
		return this.getMap(id) == null ? false : true;
	}

	public final GameMap getMap(@Nonnull String name, @Nonnull SaveType saveType) {

		GameMap map = this.cachedMaps.stream()
				.filter(m -> m.getName().equalsIgnoreCase(name) && m.getSaveType().getID() == saveType.getID())
				.findAny().orElse(null);

		// Return cached value if available
		if (map != null) {
			return map;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + this.table + " WHERE name = ? AND saveType = ?");
			stmt.setString(1, name);
			stmt.setInt(2, saveType.getID());

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				map = this.getByResultSet(rs);
				this.cache(map);

				return map;

			} else {
				return null;
			}

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not get Map with name '" + name
					+ "' and SaveType '" + saveType.getID() + "' from Database/SQL: " + e.getMessage());
			return null;
		}

	}

	public final boolean isMap(@Nonnull String name, @Nonnull SaveType saveType) {
		return this.getMap(name, saveType) == null ? false : true;
	}

	public final Collection<GameMap> getMaps() {

		Collection<GameMap> maps = new ArrayList<>();

		if (this.isMapCachingEnabled() && !this.cachedMaps.isEmpty()) {
			return this.cachedMaps;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + this.table);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				GameMap map = this.getByResultSet(rs);

				if (this.isMapCachingEnabled()) {
					this.cachedMaps.add(map);
				}

				maps.add(map);

			}

			return maps;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not get Maps from Database/SQL: " + e.getMessage());
			return null;
		}

	}

	/**
	 * 
	 * @param uuid - UUID
	 * @return All Maps where either the {@link GameMap#getCreator()} or
	 *         {@link GameMap#getContributors()} contains the given UUID
	 * @since 1.0.3
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final Collection<GameMap> getMaps(@Nonnull UUID uuid) {

		Collection<GameMap> maps = new ArrayList<>();

		if (this.isMapCachingEnabled()) {

			for (GameMap gm : this.cachedMaps) {
				if (gm.getCreator().equals(uuid) || gm.hasContributors() && gm.getContributors().contains(uuid)) {
					maps.add(gm);
				}
			}

			return maps;

		} else {

			try {

				PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement(
						"SELECT * FROM " + this.table + " WHERE creator = ? OR contributors LIKE '%?%'");
				stmt.setString(1, uuid.toString());
				stmt.setString(2, uuid.toString());

				ResultSet rs = stmt.executeQuery();

				while (rs.next()) {
					maps.add(this.getByResultSet(rs));
				}

				return maps;

			} catch (SQLException e) {
				Core.getInstance().log(this.getClass(), LogType.ERROR,
						"Could not get Maps of User '" + uuid.toString() + "'/SQL: " + e.getMessage());
				return null;
			}

		}

	}

	public final Collection<GameMap> getMaps(@Nonnull GameMapState state) {

		Collection<GameMap> maps = new ArrayList<>();

		if (this.isMapCachingEnabled()) {

			for (GameMap gm : this.cachedMaps) {
				if (gm.getState() == state) {
					maps.add(gm);
				}
			}

			return maps;

		} else {

			try {

				PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
						.prepareStatement("SELECT * FROM " + this.table + " WHERE state = ?");
				stmt.setString(1, state.toString());

				ResultSet rs = stmt.executeQuery();

				while (rs.next()) {
					maps.add(this.getByResultSet(rs));
				}

				return maps;

			} catch (SQLException e) {
				Core.getInstance().log(this.getClass(), LogType.ERROR,
						"Could not get Maps of State '" + state.toString() + "'/SQL: " + e.getMessage());
				return null;
			}

		}

	}

	public final boolean setName(@Nonnull GameMap map, @Nonnull String name) {

		name = ChatColor.stripColor(name);

		if (map.getName().equalsIgnoreCase(name))
			return false;

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + this.table + " SET name = ? WHERE id = ?");
			stmt.setString(1, name);
			stmt.setInt(2, map.getID());

			String old = map.getName();

			stmt.executeUpdate();

			if (this.isMapCachingEnabled()) {
				map.setName(name);
			}

			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Updated Name of Map '" + map.getID() + "' from '" + old + "' to '" + name + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not set Name of Map '" + map.getID() + "' to '" + name + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	public final boolean setSaveType(@Nonnull GameMap map, @Nonnull SaveType saveType) {

		if (map.getSaveType() == saveType)
			return false;

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + this.table + " SET saveType = ? WHERE id = ?");
			stmt.setInt(1, saveType.getID());
			stmt.setInt(2, map.getID());

			SaveType old = map.getSaveType();

			stmt.executeUpdate();

			if (this.isMapCachingEnabled()) {
				map.setSaveType(saveType);
			}

			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Updated SaveType of Map '" + map.getID()
					+ "' from '" + old.getName() + "' to '" + saveType.getName() + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not set saveType of Map '" + map.getID()
					+ "' to '" + saveType.getName() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	public final boolean setIcon(@Nonnull GameMap map, @Nonnull String iconName) {

		if (map.getIconName().equalsIgnoreCase(iconName))
			return false;

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + this.table + " SET icon = ? WHERE id = ?");
			stmt.setString(1, iconName);
			stmt.setInt(2, map.getID());

			String old = map.getIconName();

			stmt.executeUpdate();

			if (this.isMapCachingEnabled()) {
				map.setIconName(iconName);
			}

			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Updated Icon of Map '" + map.getID() + "' from '" + old + "' to '" + iconName + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not set Icon of Map '" + map.getID() + "' to '" + iconName + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	public final boolean setCreator(@Nonnull GameMap map, @Nonnull UUID creator) {

		if (map.getCreator().equals(creator))
			return false;

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + this.table + " SET creator = ? WHERE id = ?");
			stmt.setString(1, creator.toString());
			stmt.setInt(2, map.getID());

			String old = map.getCreator().toString();

			stmt.executeUpdate();

			if (this.isMapCachingEnabled()) {
				map.setCreator(creator);
			}

			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Updated Creator of Map '" + map.getID() + "' from '" + old + "' to '" + creator.toString() + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not set Creator of Map '" + map.getID()
					+ "' to '" + creator.toString() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	public final boolean setContributors(@Nonnull GameMap map, @Nonnull Collection<UUID> contributors) {

		if (map.hasContributors() && map.getContributors() == contributors)
			return false;

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + this.table + " SET contributors = ? WHERE id = ?");
			stmt.setString(1, GlobalUtils.getUuidString(contributors, ";"));
			stmt.setInt(2, map.getID());

			String old = GlobalUtils.getUuidString(map.getContributors(), ";");

			stmt.executeUpdate();

			if (this.isMapCachingEnabled()) {
				map.setContributors(contributors);
			}

			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Updated Contributors of Map '" + map.getID()
					+ "' from '" + old + "' to '" + GlobalUtils.getUuidString(contributors, ";") + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not set Contributors of Map '" + map.getID()
					+ "' to '" + GlobalUtils.getUuidString(contributors, ";") + "'/SQL: " + e.getMessage());
			return false;
		}

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
	public final boolean setState(@Nonnull GameMap map, @Nonnull GameMapState state) {

		if (map.getState() == state)
			return false;

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + this.table + " SET state = ? WHERE id = ?");
			stmt.setString(1, state.toString());
			stmt.setInt(2, map.getID());

			GameMapState old = map.getState();

			stmt.executeUpdate();

			if (this.isMapCachingEnabled()) {
				map.setState(state);
			}

			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Updated State of Map '" + map.getID() + "' from '"
					+ old.toString() + "' to '" + state.toString() + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not set State of Map '" + map.getID()
					+ "' to '" + state.toString() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	public final boolean setSpawns(@Nonnull GameMap map, @Nullable Collection<CustomLocation> spawns) {

//		if (map.hasSpawns() && map.getSpawns() == spawns)
//			return false;

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + this.table + " SET spawns = ? WHERE id = ?");
			stmt.setString(1, spawns == null ? null : GlobalUtils.getCustomLocationStringFromList(spawns));
			stmt.setInt(2, map.getID());

			stmt.executeUpdate();

			if (this.isMapCachingEnabled()) {
				map.setSpawns(spawns);
			}

			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Updated Spawns of Map '" + map.getID() + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not set Spawns of Map '" + map.getID() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	public final boolean setTeams(@Nonnull GameMap map, @Nonnull boolean teams) {

		if (map.isTeams() && teams || !map.isTeams() && !teams) {
//			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Did not update Teams of Map '" + map.getID() + "' because the value would be identical.");
			return false;
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("UPDATE " + this.table + " SET teams = ? WHERE id = ?");
			stmt.setBoolean(1, teams);
			stmt.setInt(2, map.getID());

			stmt.executeUpdate();

			boolean old = map.isTeams();

			if (this.isMapCachingEnabled()) {
				map.setTeams(teams);
			}

			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Updated Teams of Map '" + map.getID() + " from '" + old + "' to '" + teams + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not update Teams of Map '" + map.getID() + "'/SQL: " + e.getMessage());
			return false;
		}

	}
	
	/**
	 * This is going to <b>hard-delete</b> the Map from the Database.
	 * @param id - ID of the Map
	 * @return If the Map has been deleted
	 * @since 1.0.3
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote Use {@link #setState(GameMap, GameMapState)} with {@link GameMapState#DELETED} to mark the map as soft-deleted.
	 */
	public final boolean delete(@Nonnull int id) {
		
		GameMap map = this.getMap(id);
		
		if(map == null) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not delete Map '" + id + "' because none with that ID could be found.");
			return false;
		}
		
		try {
			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("DELETE FROM " + this.table + " WHERE id = ?");
			stmt.setInt(1, map.getID());
			
			stmt.executeUpdate();
			
			if(this.isMapCachingEnabled()) {
				this.cachedMaps.remove(map);
			}
			
			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Deleted Map '" + id + "' from Database.");
			return true;
			
		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not delete Map '" + map.getID() + "'/SQL: " + e.getMessage());
			return false;
		}
		
	}

	public final void cache(@Nonnull GameMap map) {

		if (!this.isMapCachingEnabled())
			return;
		if (this.cachedMaps.contains(map))
			return;

		this.cachedMaps.add(map);
	}

	private final GameMap getByResultSet(@Nonnull ResultSet rs) throws SQLException {

		int id = rs.getInt(1);
		String name = rs.getString(2);
		SaveType saveType = Core.getInstance().getSaveTypeByID(rs.getInt(3));
		String iconName = rs.getString(4);
		UUID creator = UUID.fromString(rs.getString(5));

		Collection<UUID> contributors = new ArrayList<>();
		if (rs.getString(6) != null && !rs.getString(6).equals("")) {
			contributors = GlobalUtils.getUuidListByString(rs.getString(6), ";");
		}

		GameMapState state = GameMapState.valueOf(rs.getString(7));

		Collection<CustomLocation> spawns = new ArrayList<>();
		if (rs.getString(8) != null && !rs.getString(8).equals("")) {
			spawns = GlobalUtils.getCustomLocationListFromString(rs.getString(8));
		}

		boolean teams = rs.getBoolean(9);

		return new GameMap(id, name, saveType, iconName, creator, contributors, state, spawns, teams);
	}

	public final String getContributorsAsString(@Nonnull GameMap map) {

		StringBuilder sb = new StringBuilder();

		if (map.getContributors() == null || map.getContributors().isEmpty()) {
			sb.append(ChatColor.GRAY + "None");
		} else {

			int current = 0;
			int max = map.getContributors().size();

			for (UUID uuid : map.getContributors()) {

				current++;
				User user = Core.getInstance().getUserSystem().getUser(uuid);
				sb.append(user.getDisplayName() + (current >= max ? "" : ChatColor.GRAY + ", "));

			}

		}

		return sb.toString();
	}

}
