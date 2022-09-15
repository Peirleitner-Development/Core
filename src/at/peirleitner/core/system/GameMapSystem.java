package at.peirleitner.core.system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nonnull;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.CustomLocation;
import at.peirleitner.core.util.GlobalUtils;
import at.peirleitner.core.util.LogType;
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

	private final String table = Core.getInstance().getTableMaps();
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

		try {

			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + this.table);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				GameMap map = this.getByResultSet(rs);
				this.cachedMaps.add(map);

			}

			Core.getInstance().log(this.getClass(), LogType.INFO,
					"Cached " + this.cachedMaps.size() + " Maps from Database.");

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not load Maps from Database/SQL: " + e.getMessage());
			return;
		}

	}

	public final boolean isMapCachingEnabled() {
		return Core.getInstance().getSettingsManager().isSetting(Core.getInstance().getPluginName(), "cache-game-maps");
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
			spawns = GlobalUtils.getCustomLocationListFromString(rs.getString(8), ":");
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
