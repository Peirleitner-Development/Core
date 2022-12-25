package at.peirleitner.core.util.user;

import java.util.UUID;

import at.peirleitner.core.system.ExperienceSystem;
import at.peirleitner.core.util.database.SaveType;

/**
 * This class represents a result of the {@link ExperienceSystem}
 * 
 * @since 1.0.18
 * @author Markus Peirleitner (Rengobli)
 *
 */
public final class UserExperience {

	private final UUID uuid;
	private final SaveType saveType;
	private int level;
	private int experience;
	private int prestige;

	public UserExperience(UUID uuid, SaveType saveType, int level, int experience, int prestige) {
		this.uuid = uuid;
		this.saveType = saveType;
		this.level = level;
		this.experience = experience;
		this.prestige = prestige;
	}

	public final UUID getUUID() {
		return uuid;
	}

	public final SaveType getSaveType() {
		return saveType;
	}

	public final int getLevel() {
		return level;
	}

	public final void setLevel(int level) {
		this.level = level;
	}

	public final int getExperience() {
		return experience;
	}

	public final void setExperience(int experience) {
		this.experience = experience;
	}

	public final int getPrestige() {
		return prestige;
	}

	public final void setPrestige(int prestige) {
		this.prestige = prestige;
	}
	
	public final boolean isDefault() {
		return this.getExperience() == 0 && this.getLevel() == 1 && this.getPrestige() == 0;
	}

	@Override
	public String toString() {
		return "UserExperience [uuid=" + uuid + ", saveType=" + saveType + ", level=" + level + ", experience="
				+ experience + ", prestige=" + prestige + "]";
	}

}
