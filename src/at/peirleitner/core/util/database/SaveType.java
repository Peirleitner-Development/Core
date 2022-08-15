package at.peirleitner.core.util.database;

import javax.annotation.Nonnull;

public class SaveType {

	private final int id;
	private final String name;
	private final String iconName;

	public SaveType(@Nonnull int id, @Nonnull String name, @Nonnull String iconName) {
		this.id = id;
		this.name = name;
		this.iconName = iconName;
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

}
