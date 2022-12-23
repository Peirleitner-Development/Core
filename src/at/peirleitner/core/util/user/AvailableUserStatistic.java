package at.peirleitner.core.util.user;

import at.peirleitner.core.util.database.SaveType;

/**
 * This class defines an available user statistic
 * 
 * @since 1.0.17
 * @author Markus Peirleitner (Rengobli)
 * @see UserStatistic
 */
public class AvailableUserStatistic {

	private int id;
	private String devName;
	private String displayName;
	private String description;
	private SaveType saveType;
	private long created;
	private boolean isEnabled;
	private String iconName;

	public AvailableUserStatistic() {

	}

	public final int getID() {
		return id;
	}

	public final void setID(int id) {
		this.id = id;
	}

	public final String getDevName() {
		return devName;
	}

	public final void setDevName(String devName) {
		this.devName = devName;
	}

	public final String getDisplayName() {
		return displayName;
	}

	public final void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public final String getDescription() {
		return description;
	}

	public final void setDescription(String description) {
		this.description = description;
	}

	public final SaveType getSaveType() {
		return saveType;
	}

	public final void setSaveType(SaveType saveType) {
		this.saveType = saveType;
	}

	public final long getCreated() {
		return created;
	}

	public final void setCreated(long created) {
		this.created = created;
	}

	public final boolean isEnabled() {
		return isEnabled;
	}

	public final void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public final String getIconName() {
		return iconName;
	}

	public final void setIconName(String iconName) {
		this.iconName = iconName;
	}

	@Override
	public String toString() {
		return "AvailableUserStatistic [id=" + id + ", devName=" + devName + ", displayName=" + displayName
				+ ", description=" + description + ", saveType=" + saveType + ", created=" + created + ", isEnabled="
				+ isEnabled + ", iconName=" + iconName + "]";
	}

}
