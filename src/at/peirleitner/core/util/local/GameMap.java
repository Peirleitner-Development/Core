package at.peirleitner.core.util.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;

import at.peirleitner.core.util.database.SaveType;

public class GameMap implements GameMapData {

	private String name;
	private SaveType saveType;
	private Material icon;
	private UUID creator;
	private Collection<UUID> contributors;
	private GameMapState state;
	private Collection<CustomLocation> spawns;
	private boolean isTeams;

	public GameMap(@Nonnull String name, @Nonnull SaveType saveType, @Nullable Material icon, @Nonnull UUID creator,
			@Nullable List<UUID> contributors, @Nonnull GameMapState state, @Nullable List<CustomLocation> spawns,
			@Nonnull boolean isTeams) {
		this.name = name;
		this.saveType = saveType;
		this.icon = (icon == null ? this.getDefaultIcon() : icon);
		this.creator = creator;
		this.contributors = (contributors == null ? new ArrayList<>() : contributors);
		this.state = state;
		this.spawns = (spawns == null ? new ArrayList<>() : spawns);
		this.isTeams = isTeams;
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
	public final Material getIcon() {
		return icon;
	}

	public final void setIcon(@Nonnull Material icon) {
		this.icon = icon;
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
		return !this.getContributors().isEmpty();
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
		return !this.getSpawns().isEmpty();
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

}
