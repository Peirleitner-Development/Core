package at.peirleitner.core.util.database;

import javax.annotation.Nonnull;

import at.peirleitner.core.util.user.User;

/**
 * A SaveType is used to separate data inside the database to avoid
 * overrides.<br>
 * Example: The {@link User} may have A$ on SaveType 1, as well as B$ on
 * SaveType 2 - On both SaveTypes the same Database/Table is used.
 * 
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class SaveType {

	private final int id;
	private final String name;
	private final String iconName;
	private final WorldType worldType;

	public SaveType(@Nonnull int id, @Nonnull String name, @Nonnull String iconName, @Nonnull WorldType worldType) {
		this.id = id;
		this.name = name;
		this.iconName = iconName;
		this.worldType = worldType;
	}

	public final int getID() {
		return id;
	}

	public final String getName() {
		return name;
	}

	public final String getIconName() {
		return iconName;
	}

	public final WorldType getWorldType() {
		return worldType;
	}

	@Override
	public final String toString() {
		return "SaveType[id=" + id + ",name=" + name + ",iconName=" + iconName + ",worldType=" + worldType + "]";
	}

	/**
	 * @since 1.0.3
	 * @author Markus Peirleitner (Rengobli)
	 */
	public enum WorldType {
		NORMAL, FLAT, NETHER, END, VOID;
	}

}
