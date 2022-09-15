package at.peirleitner.core.util.local;

import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Material;

import at.peirleitner.core.util.CustomLocation;
import at.peirleitner.core.util.database.SaveType;

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

	public final void setName(@Nonnull String name) {
		this.name = name;
	}

	@Override
	public final SaveType getSaveType() {
		return saveType;
	}

	public final void setSaveType(@Nonnull SaveType saveType) {
		this.saveType = saveType;
	}

	@Override
	public final String getIconName() {
		return iconName;
	}

	public final void setIconName(@Nonnull String iconName) {
		this.iconName = iconName;
	}

	public final Material getDefaultIcon() {
		return Material.PAPER;
	}

	@Override
	public final UUID getCreator() {
		return creator;
	}

	public final void setCreator(UUID creator) {
		this.creator = creator;
	}

	@Override
	public final Collection<UUID> getContributors() {
		return contributors;
	}

	public final void setContributors(Collection<UUID> contributors) {
		this.contributors = contributors;
	}

	public final boolean hasContributors() {
		return this.getContributors() == null || this.getContributors().isEmpty() ? false : true;
	}

	@Override
	public final GameMapState getState() {
		return state;
	}

	public final void setState(GameMapState state) {
		this.state = state;
	}

	@Override
	public final Collection<CustomLocation> getSpawns() {
		return spawns;
	}

	public final void setSpawns(Collection<CustomLocation> spawns) {
		this.spawns = spawns;
	}

	public final boolean hasSpawns() {
		return this.getSpawns() == null || this.getSpawns().isEmpty() ? false : true;
	}

	@Override
	public final boolean isTeams() {
		return isTeams;
	}

	public final void setTeams(boolean isTeams) {
		this.isTeams = isTeams;
	}

	public final boolean updateToDatabase() {
		return false;
	}

	@Override
	/**
	 * @since 1.0.3
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String toString() {
		return "GameMap[id=" + id + ",name=" + name + ",saveType={" + saveType.toString() + "},icon=" + iconName
				+ ",creator=" + creator.toString() + ",contributors={"
				+ (!hasContributors() ? "null" : contributors.toString()) + "},state=" + state.toString() + ",spawns={"
				+ (!hasSpawns() ? "null" : spawns.toString()) + "},teams=" + isTeams + "]";
	}

}
