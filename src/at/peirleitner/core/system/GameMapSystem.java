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
		
		if(!this.isMapCachingEnabled()) {
			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Did not load Maps because caching has been disabled.");
			return;
		}
		
		try {
			
			PreparedStatement stmt = Core.getInstance().getMySQL().getConnection().prepareStatement("SELECT * FROM " + this.table);
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()) {
				
				GameMap map = this.getByResultSet(rs);
				this.cachedMaps.add(map);
				
			}
			
			Core.getInstance().log(this.getClass(), LogType.INFO, "Cached " + this.cachedMaps.size() + " Maps from Database.");
			
		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not load Maps from Database/SQL: " + e.getMessage());
			return;
		}
		
	}
	
	public final boolean isMapCachingEnabled() {
		return Core.getInstance().getSettingsManager().isSetting(Core.getInstance().getPluginName(), "cache-game-maps");
	}

	private final GameMap getFromCache(@Nonnull int id) {
		return this.cachedMaps.stream().filter(map -> map.getID() == id).findAny().orElse(null);
	}
	
	public final GameMap getMap(@Nonnull int id) {
		return this.getFromCache(id) == null ? this.getFromDatabase(id) : this.getFromCache(id);
	}

	private final void cache(@Nonnull GameMap map) {

		if (!this.isMapCachingEnabled())
			return;
		if (this.cachedMaps.contains(map))
			return;

		this.cachedMaps.add(map);
	}

	private final GameMap getFromDatabase(@Nonnull int id) {

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
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not get GameMap with ID '" + id + "' from Database/SQL: " + e.getMessage());
			return null;
		}

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

}
