package at.peirleitner.core.system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.CoreSystem;
import at.peirleitner.core.util.CustomLocation;
import at.peirleitner.core.util.GlobalUtils;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.database.SaveType;
import at.peirleitner.core.util.database.TableType;
import at.peirleitner.core.util.local.GameMap;
import at.peirleitner.core.util.local.GameMapState;
import at.peirleitner.core.util.user.User;
import net.md_5.bungee.api.ChatColor;

/**
 * System used to interact with {@link GameMap}s
 * 
 * @since 1.0.3, Updated in 1.0.20
 * @author Markus Peirleitner (Rengobli)
 *
 */
public final class GameMapSystem implements CoreSystem {

	private final String table = TableType.MAPS.getTableName(true);
	private Set<GameMap> cachedMaps;

	public GameMapSystem() {

		// Initialize
		this.cachedMaps = new HashSet<>();

		// Load Data
		this.loadMaps();

	}

	/**
	 * Initially loads all Maps using {@link #getMapsFromDatabase()}
	 * @since 1.0.3
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote This will not work if {@link #getCachedMaps()} isn't empty.
	 */
	private final void loadMaps() {

		if (!this.isMapCachingEnabled()) {
			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Did not load Maps because caching has been disabled.");
			return;
		}
		
		if (this.getCachedMaps().length < 1) {
			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Did not load Maps because the cache isn't empty.");
			return;
		}

		Set<GameMap> maps = new HashSet<>();
		
		for(GameMap map : this.getMapsFromDatabase()) {
			maps.add(map);
		}
		
		this.cachedMaps = maps;
		Core.getInstance().log(this.getClass(), LogType.INFO, "Cached " + maps.size() + " Maps from Database.");

	}

	/**
	 * 
	 * @return Cached Maps
	 * @since 1.0.20
	 * @author Markus Peirleitner (Rengobli)
	 * @see #getMaps()
	 */
	private final GameMap[] getCachedMaps() {

		GameMap[] array = new GameMap[this.cachedMaps.size()];
		return this.cachedMaps.toArray(array);

	}

	private final GameMap[] getArray(@Nonnull Set<GameMap> maps) {
		GameMap[] array = new GameMap[maps.size()];
		return maps.toArray(array);
	}

	public final boolean isMapCachingEnabled() {
		return Core.getInstance().getSettingsManager().isSetting(Core.getInstance().getPluginName(),
				"manager.settings.cache-game-maps");
	}

	public final GameMap getMap(@Nonnull int id) {

		if (this.isMapCachingEnabled()) {
			return this.cachedMaps.stream().filter(m -> m.getID() == id).findAny().orElse(null);
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + this.table + " WHERE id = ?");
			stmt.setInt(1, id);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				GameMap map = this.getByResultSet(rs);
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

		if (this.isMapCachingEnabled()) {
			return this.cachedMaps.stream()
					.filter(m -> m.getName().equalsIgnoreCase(name) && m.getSaveType().getID() == saveType.getID())
					.findAny().orElse(null);
		}

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + this.table + " WHERE name = ? AND saveType = ?");
			stmt.setString(1, name);
			stmt.setInt(2, saveType.getID());

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				GameMap map = this.getByResultSet(rs);
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

	/**
	 * 
	 * @return All available Maps. If {@link #isMapCachingEnabled()} is set to
	 *         <code>true</code>, this returns {@link #getCachedMaps()}.
	 * @see #getCachedMaps()
	 */
	public final GameMap[] getMaps() {

		if (this.isMapCachingEnabled() && !this.cachedMaps.isEmpty()) {
			return this.getCachedMaps();
		}

		return this.getMapsFromDatabase();

	}

	/**
	 * Get all Maps from Database and cache them directly.
	 * 
	 * @return Maps from Database
	 * @since 1.0.20
	 * @author Markus Peirleitner (Rengobli)
	 */
	private final GameMap[] getMapsFromDatabase() {

		Set<GameMap> maps = new HashSet<>();

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

			return this.getArray(maps);

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
	public final GameMap[] getMaps(@Nonnull UUID uuid) {

		Set<GameMap> maps = new HashSet<>();

		// Return from Cache
		if (this.isMapCachingEnabled()) {

			for (GameMap gm : this.cachedMaps) {
				if (gm.getCreator().equals(uuid) || gm.hasContributors() && gm.getContributors().contains(uuid)) {
					maps.add(gm);
				}
			}

			return this.getArray(maps);

		}

		// Return from Database
		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + this.table + " WHERE creator = ? OR contributors LIKE '%?%'");
			stmt.setString(1, uuid.toString());
			stmt.setString(2, uuid.toString());

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				maps.add(this.getByResultSet(rs));
			}

			return this.getArray(maps);

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not get Maps of User '" + uuid.toString() + "'/SQL: " + e.getMessage());
			return null;
		}

	}

	public final GameMap[] getMaps(@Nonnull GameMapState state) {

		Set<GameMap> maps = new HashSet<>();

		// Return from Cache
		if (this.isMapCachingEnabled()) {

			for (GameMap gm : this.cachedMaps) {
				if (gm.getState() == state) {
					maps.add(gm);
				}
			}

			return this.getArray(maps);

		}

		// Return from Database
		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + this.table + " WHERE state = ?");
			stmt.setString(1, state.toString());

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				maps.add(this.getByResultSet(rs));
			}

			return this.getArray(maps);

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not get Maps of State '" + state.toString() + "'/SQL: " + e.getMessage());
			return null;
		}

	}

	/**
	 * This is going to <b>hard-delete</b> the Map from the Database.
	 * 
	 * @param id - ID of the Map
	 * @return If the Map has been deleted
	 * @since 1.0.3
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote Use {@link #setState(GameMap, GameMapState)} with
	 *          {@link GameMapState#DELETED} to mark the map as soft-deleted.
	 */
	public final boolean delete(@Nonnull int id) {

		GameMap map = this.getMap(id);

		if (map == null) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not delete Map '" + id + "' because none with that ID could be found.");
			return false;
		}

		try {
			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("DELETE FROM " + this.table + " WHERE id = ?");
			stmt.setInt(1, map.getID());

			stmt.executeUpdate();

			if (this.isMapCachingEnabled()) {
				this.cachedMaps.remove(map);
			}

			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Deleted Map '" + id + "' from Database.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not delete Map '" + map.getID() + "'/SQL: " + e.getMessage());
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

		Set<UUID> contributors = new HashSet<>();
		if (rs.getString(6) != null && !rs.getString(6).equals("")) {
			contributors = GlobalUtils.getUuidSetByString(rs.getString(6), ";");
		}

		GameMapState state = GameMapState.valueOf(rs.getString(7));

		Set<CustomLocation> spawns = new HashSet<>();
		if (rs.getString(8) != null && !rs.getString(8).equals("")) {
			spawns = GlobalUtils.getCustomLocationSetFromString(rs.getString(8));
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

	@Override
	public void createTable() {
		return;
	}

	@Override
	public TableType getTableType() {
		return TableType.MAPS;
	}

}
