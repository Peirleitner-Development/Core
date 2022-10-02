package at.peirleitner.core.util.local;

import java.util.Collection;
import java.util.UUID;

import at.peirleitner.core.util.CustomLocation;
import at.peirleitner.core.util.database.SaveType;

/**
 * This interface specifies data of a GameMap
 * 
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 *
 */
public interface GameMapData {

	/**
	 * 
	 * @return Unique ID of this Map
	 * @since 1.0.3
	 * @author Markus Peirleitner (Rengobli)
	 */
	public int getID();

	/**
	 * 
	 * @return Name
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public String getName();

	/**
	 * 
	 * @return SaveType that this Map has been built for
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public SaveType getSaveType();

	/**
	 * 
	 * @return Icon for {@link GUI}s
	 * @since 1.0.3
	 * @author Markus Peirleitner (Rengobli)
	 */
	public String getIconName();

	/**
	 * 
	 * @return Map Creator
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public UUID getCreator();

	/**
	 * 
	 * @return Contributors
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public Collection<UUID> getContributors();

	/**
	 * 
	 * @return Current State of the Map
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public GameMapState getState();

	/**
	 * 
	 * @return Collection of Spawn Locations
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public Collection<CustomLocation> getSpawns();

	/**
	 * 
	 * @return If this Map supports teams
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public boolean isTeams();

}
